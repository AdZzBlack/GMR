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
class Maps extends REST_Controller { 

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
	
	function getMaps_post(){     
		$data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomor_bangunan          = (isset($jsonObject["nomor_bangunan"])  	? $jsonObject["nomor_bangunan"]                    : "29");
		
		$query = "	SELECT 
						a.nama,
						a.koordinat, 
						FC_GENERATE_IMAGESITEPLAN(a.nomor) AS denah, 
						FC_GENERATE_PROGRESSBANGUNAN(a.nomor) AS progressbangunan, 
						FC_GENERATE_PROGRESSNAMA(a.nomor) AS progressnama, 
						a.tipe AS isparent  
					FROM mhbangunan a 
					WHERE a.status_aktif = 1 AND a.nomor = ".$nomor_bangunan;
	
		$result = $this->db->query($query);
	
		if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
				$childName = "0";
				$childCoordinat = "0";
				$childProgressBangunan = "0";
				$childProgressNama = "0";
				if($r['isparent']==1)
				{
					$query1 = "SELECT GROUP_CONCAT(nama SEPARATOR '@') AS nama, 
									GROUP_CONCAT(koordinat SEPARATOR '@') AS koordinat, 
									GROUP_CONCAT(peta_progress SEPARATOR '@') AS peta_progress, 
									GROUP_CONCAT(progress_bangunan SEPARATOR '@') AS progress_bangunan, 
									GROUP_CONCAT(progress_nama SEPARATOR '@') AS progress_nama, 
									GROUP_CONCAT(child_nama_bangunan SEPARATOR '@') AS child_nama_bangunan
							FROM (
								SELECT a.nomorheader,
										a.nama, 
										a.koordinat, 
										FC_GENERATE_PROGRESSBANGUNAN_GRADIENT(a.nomor) AS peta_progress, 
										FC_GENERATE_PROGRESSBANGUNAN(a.nomor) AS progress_bangunan, 
										FC_GENERATE_PROGRESSNAMA(a.nomor) AS progress_nama,
										FC_GENERATE_BANGUNAN_CHILD_NAMA(a.nomor) AS child_nama_bangunan
								FROM mhbangunan a 
								WHERE a.status_aktif > 0 AND a.koordinat IS NOT NULL 
							) a  
							WHERE a.nomorheader = ".$nomor_bangunan;
	
					$result1 = $this->db->query($query1);
					
					if( $result1 && $result1->num_rows() > 0){
						foreach ($result1->result_array() as $r1){
							$childName = $r1['nama'];
							$childCoordinat = $r1['koordinat'];
							$childProgressBangunan = $r1['progress_bangunan'];
							$childProgressNama = $r1['progress_nama'];
						}
					}
				}
				
				array_push($data['data'], array(
												'name'		 	   		=> $r['nama'], 
												'koordinat' 	   		=> $r['koordinat'], 
												'denah'		  			=> $r['denah'],
												'progressbangunan'		=> $r['progressbangunan'],
												'progressnama'			=> $r['progressnama'],
												'isparent'      		=> $r['isparent'],
												'childName'				=> $childName,
												'childkoordinat'		=> $childCoordinat,
												'childprogressbangunan'	=> $childProgressBangunan,
												'childprogressnama'		=> $childProgressNama
												)
				);
            }
        }else{      
            array_push($data['data'], array( 'query' => $this->error($query) ));
        } 
		
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
	}
}
