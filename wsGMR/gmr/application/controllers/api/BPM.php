<?php

defined('BASEPATH') OR exit('No direct script access allowed');

// This can be removed if you use __autoload() in config.php OR use Modular Extensions
require APPPATH . '/libraries/REST_Controller.php';

/**
 * This is an example of a few basic event interaction methods you could use
 * all done with a hardcoded array
 *
 * @package         CodeIgniter
 * @subpackage      Rest Server
 * @category        Controller
 * @author          Phil Sturgeon, Chris Kacerguis
 * @license         MIT
 * @link            https://github.com/chriskacerguis/codeigniter-restserver
 */
class BPM extends REST_Controller { 

    function __construct()
    {
        // Construct the parent class
        parent::__construct();

        // Configure limits on our controller methods
        // Ensure you have created the 'limits' table and enabled 'limits' within application/config/rest.php
        $this->methods['event_post']['limit'] = 500000000; // 500 requests per hour per event/key
        // $this->methods['event_delete']['limit'] = 50; // 50 requests per hour per event/key
        $this->methods['event_get']['limit'] = 500000000; // 500 requests per hour per event/key

        header("Access-Control-Allow-Origin: *");
        header("Access-Control-Allow-Methods: GET, POST");
        header("Access-Control-Allow-Headers: Origin, Content-Type, Accept, Authorization");
    }

    function ellipsis($string) {
        $cut = 30;
        $out = strlen($string) > $cut ? substr($string,0,$cut)."..." : $string;
        return $out;
    }

    function clean($string) {
        return preg_replace("/[^[:alnum:][:space:]]/u", '', $string); // Replaces multiple hyphens with single one.
    }

    function error($string) {
        return str_replace( array("\t", "\n", "\r") , "", $string);
    }

    function getGCMId($user_nomor){
        $query = "  SELECT 
                    a.gcm_id AS gcmid
                    FROM mhadmin a
                    WHERE a.status_aktif > 0 AND (a.gcm_id <> '' AND a.gcm_id IS NOT NULL) AND a.nomor = $user_nomor ";
        return $this->db->query($query)->row()->gcmid;
    }

    public function send_gcm($registrationId,$message,$title,$fragment,$nomor,$nama)
    {
        $this->load->library('gcm');

        $this->gcm->setMessage($message);
        $this->gcm->setTitle($title);
        $this->gcm->setFragment($fragment);
        $this->gcm->setNomor($nomor);
        $this->gcm->setNama($nama);

        $this->gcm->addRecepient($registrationId);

        $this->gcm->setData(array(
            'some_key' => 'some_val'
        ));

        $this->gcm->setTtl(500);
        $this->gcm->setTtl(false);

        $this->gcm->setGroup('Test');
        $this->gcm->setGroup(false);

        $this->gcm->send();

		/*
        if ($this->gcm->send())
            echo 'Success for all messages';
        else
            echo 'Some messages have errors';

        print_r($this->gcm->status);
        print_r($this->gcm->messagesStatuses);

        die(' Worked.');
		*/
    }
	
	public function send_gcm_group($registrationId,$message,$title,$fragment,$nomor,$nama)
    {
        $this->load->library('gcm');

        $this->gcm->setMessage($message);
        $this->gcm->setTitle($title);
        $this->gcm->setFragment($fragment);
        $this->gcm->setNomor($nomor);
        $this->gcm->setNama($nama);

        foreach ($registrationId as $regisID) {
            $this->gcm->addRecepient($regisID);
        }

        $this->gcm->setTtl(500);
        $this->gcm->setTtl(false);

        $this->gcm->setGroup('Test');
        $this->gcm->setGroup(false);

        $this->gcm->send();
    }
	
	function test_get()
	{
		
		$regisID = array();
				
				$query_getuser = " SELECT 
									a.gcmid
									FROM whuser_mobile a 
									JOIN whrole_mobile b ON a.nomorrole = b.nomor
									WHERE a.status_aktif > 0 AND (a.gcmid <> '' AND a.gcmid IS NOT NULL) AND b.approvedeliveryorder = 1 ";
				$result_getuser = $this->db->query($query_getuser);

				if( $result_getuser && $result_getuser->num_rows() > 0){
					foreach ($result_getuser->result_array() as $r_user){

						// START SEND NOTIFICATION
						$vcGCMId = $r_user['gcmid'];
						if( $vcGCMId != "null" ){      
							array_push($regisID, $vcGCMId);       
						}
						
					}
					$count = $this->db->query("SELECT COUNT(1) AS order_baru FROM thdeliveryorder a WHERE a.status_disetujui = 0")->row()->order_baru; 
					$this->send_gcm_group($regisID, $this->ellipsis($count . ' pending order'),'Delivery Order','ChooseApprovalDelivery','','');
				} 
	}
	
	function createBPM_post(){     
		$data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomor_user          = (isset($jsonObject["nomor_user"])  	? $jsonObject["nomor_user"]                    : "");
		$tanggal             = (isset($jsonObject["tanggal"]) 		? $jsonObject["tanggal"]                       : "");
		$nomor_thDO		     = (isset($jsonObject["nomor_thDO"]) 	? $jsonObject["nomor_thDO"] 	               : "");
		$dataBPM             = (isset($jsonObject["dataBPM"])  		? $jsonObject["dataBPM"]                       : "");
		$photo               = (isset($jsonObject["photo"])  		? $jsonObject["photo"]                         : "");
		
		$this->db->trans_begin();
		
		if($dataBPM != "")
		{
			$query = "INSERT INTO thbpm(`nomorthdeliveryorder`, `dibuat_pada`, `dibuat_oleh`, `kode`, `photo`) VALUES($nomor_thDO, '$tanggal', $nomor_user, FC_GENERATE_BPM_KODE(), '$photo')";
		
			$this->db->query($query);

			$header_nomor = $this->db->insert_id();
			
			$pieces = explode("|", $dataBPM);
			foreach ($pieces as $arr) {
                $valuedata = explode("~", $arr);

                if( $valuedata[0] != ""){
					$query_detail_bpm = $this->db->insert_string('tdbpm', array(
                                                                          'nomorthbpm'			=>$header_nomor,
																		  'nomortddeliveryorder'=>$valuedata[0], 
                                                                          'nomormhbarang'     	=>$valuedata[3], 
                                                                          'jumlahkirim' 		=>$valuedata[1], 
                                                                          'harga' 				=>$valuedata[2],
																		  'keterangan'			=>$valuedata[5],
																		  'dibuat_oleh'			=>$nomor_user
                                                                        )
                                                    );
                    $this->db->query($query_detail_bpm);
					
                }
            }
		}
		
		
		if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'failed' => $query_detail_bpm ));
        }else{
            $this->db->trans_commit();
			
			$kode = $this->db->query("SELECT kode FROM thbpm where nomor = $header_nomor")->row()->kode;
			$tanggal = $this->db->query("SELECT dibuat_pada FROM thbpm where nomor = $header_nomor")->row()->dibuat_pada;
			$bangunan = $this->db->query("SELECT b.nama FROM thdeliveryorder a JOIN mhbangunan_view b ON a.nomormhbangunan = b.nomor where a.nomor = $nomor_thDO")->row()->nama;
			$nomor_project = $this->db->query("SELECT b.nomorproject FROM thdeliveryorder a JOIN mhbangunan_view b ON a.nomormhbangunan = b.nomor where a.nomor = $nomor_thDO")->row()->nomorproject;
			$project = $this->db->query("SELECT nama FROM mhbangunan WHERE nomor = $nomor_project")->row()->nama;
			
            array_push($data['data'], array( 
										'success' => 'success',
										'nomor'	=> $header_nomor,
										'kode' => $kode,
										'tanggal' => $tanggal,
										'bangunan' => $bangunan,
										'project' => $project,
								));
        } 
		
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
	}
}
