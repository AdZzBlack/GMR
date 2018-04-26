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
class Login extends REST_Controller { 

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
        return str_replace( array("\t", "\n") , "", $string);
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
		
		$result = "a";
		
		$data['data'] = array();
		
		// START SEND NOTIFICATION
        $vcGCMId = $this->getGCMId(3);
		
        $this->send_gcm($vcGCMId, $this->ellipsis('$new_message'),'New Message(s) From ','PrivateMessage','0','0');
        
		
		
		$this->response($vcGCMId);
		
		/*
		$regisID = array();
			
		$query_getuser = " SELECT 
							a.gcmid
							FROM whuser_mobile a 
							JOIN whrole_mobile b ON a.nomorrole = b.nomor
							WHERE a.status_aktif > 0 AND (a.gcmid <> '' AND a.gcmid IS NOT NULL) AND b.approveberitaacara = 1 ";
		$result_getuser = $this->db->query($query_getuser);

		if( $result_getuser && $result_getuser->num_rows() > 0){
			foreach ($result_getuser->result_array() as $r_user){

				// START SEND NOTIFICATION
				$vcGCMId = $r_user['gcmid'];
				if( $vcGCMId != "null" ){      
					array_push($regisID, $vcGCMId);       
				}
				
			}
			$count = $this->db->query("SELECT COUNT(1) AS elevasi_baru FROM mhberitaacara a WHERE a.status_disetujui = 0")->row()->elevasi_baru; 
			$this->send_gcm_group($regisID, $this->ellipsis("Berita Acara Elevasi"),$count . ' pending elevasi','ChooseApprovalElevasi','','');
		} 
		*/
	}

	// --- POST Login --- //
	function loginUser_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $user = strtolower((isset($jsonObject["username"]) ? $this->clean($jsonObject["username"])     : ""));
        $pass = md5((isset($jsonObject["password"]) ? $this->clean($jsonObject["password"]) : ""));

		$query1 = "	UPDATE mhadmin a
					SET a.hash = UUID()
					WHERE a.status_aktif > 0 
					AND LOWER(a.kode) = LOWER('$user')
					AND BINARY a.sandi = '$pass'";
        $this->db->query($query1);

        $query = "	SELECT 
						a.kode AS user_id,
						a.nomor AS user_nomor,
						a.nama AS user_nama,
						b.nama AS user_role,
						a.hash AS user_hash,
						a.nomormhcabang AS user_cabang,
						b.beritaacara AS role_beritaacara,
						b.approveberitaacara AS role_approveberitaacara,
						b.deliveryorder AS role_deliveryorder,
						b.approvedeliveryorder AS role_approvedeliveryorder,
						b.bpm AS role_bpm,
						b.opname AS role_opname,
						b.viewnotabeli AS role_viewnotabeli,
						b.createnotabeli AS role_notabeli,
						b.map AS role_map,
						b.pasang AS role_pasang,
						b.crossbranch AS role_crossbranch
					FROM mhadmin a
					JOIN whrole_mobile b
						ON a.role_android = b.nomor
					WHERE a.status_aktif > 0 
					AND LOWER(a.kode) = LOWER('$user')
					AND BINARY a.sandi = '$pass'";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                								'user_id' 						=> $r['user_id'],
												'user_nomor' 					=> $r['user_nomor'],
                                                'user_nama'             		=> $r['user_nama'], 
                								'user_role' 					=> $r['user_role'], 
												'user_hash' 					=> $r['user_hash'], 
                								'user_cabang' 				    => $r['user_cabang'],
                								'role_beritaacara' 				=> $r['role_beritaacara'],
                								'role_approveberitaacara' 		=> $r['role_approveberitaacara'], 
                                                'role_deliveryorder'            => $r['role_deliveryorder'], 
                								'role_approvedeliveryorder'     => $r['role_approvedeliveryorder'],
												'role_bpm'					    => $r['role_bpm'],
												'role_opname'				    => $r['role_opname'],
												'role_viewnotabeli'				=> $r['role_viewnotabeli'],
												'role_notabeli'				    => $r['role_notabeli'],
												'role_map'				    	=> $r['role_map'],
												'role_pasang'				    => $r['role_pasang'],
												'role_crossbranch'				=> $r['role_crossbranch']
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
	
	function logoutUser_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"])     : "");

		$this->db->trans_begin();
		
		$query = "	UPDATE mhadmin a
					SET hash = '',
					gcm_id = ''
					WHERE a.status_aktif > 0 
					AND a.nomor = $nomor";
        $this->db->query($query);
		
        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => $query ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }
	
	// --- POST Check Login --- //
	function checkLogin_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $hash = (isset($jsonObject["hash"]) ? "'" . $jsonObject["hash"] . "'"     : "");
		
        $query = "	SELECT 
						a.kode AS user_id,
                        a.nomor AS user_nomor,
                        a.nama AS user_nama,
                        b.nama AS user_role,
                        a.hash AS user_hash,
                        a.nomormhcabang AS user_cabang,
                        b.beritaacara AS role_beritaacara,
                        b.approveberitaacara AS role_approveberitaacara,
                        b.deliveryorder AS role_deliveryorder,
                        b.approvedeliveryorder AS role_approvedeliveryorder,
                        b.bpm AS role_bpm,
                        b.opname AS role_opname,
                        b.viewnotabeli AS role_viewnotabeli,
                        b.createnotabeli AS role_notabeli,
                        b.map AS role_map,
                        b.pasang AS role_pasang,
                        b.crossbranch AS role_crossbranch
					FROM mhadmin a
					JOIN whrole_mobile b
						ON a.role_android = b.nomor
					WHERE a.hash = $hash";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0)
		{
			foreach ($result->result_array() as $r)
			{
				array_push($data['data'], array(
													'success'						=> "true",
													'user_id' 						=> $r['user_id'],
                                                    'user_nomor' 					=> $r['user_nomor'],
                                                    'user_nama'             		=> $r['user_nama'],
                                                    'user_role' 					=> $r['user_role'],
                                                    'user_hash' 					=> $r['user_hash'],
                                                    'user_cabang' 				    => $r['user_cabang'],
                                                    'role_beritaacara' 				=> $r['role_beritaacara'],
                                                    'role_approveberitaacara' 		=> $r['role_approveberitaacara'],
                                                    'role_deliveryorder'            => $r['role_deliveryorder'],
                                                    'role_approvedeliveryorder'     => $r['role_approvedeliveryorder'],
                                                    'role_bpm'					    => $r['role_bpm'],
                                                    'role_opname'				    => $r['role_opname'],
                                                    'role_viewnotabeli'				=> $r['role_viewnotabeli'],
                                                    'role_notabeli'				    => $r['role_notabeli'],
                                                    'role_map'				    	=> $r['role_map'],
                                                    'role_pasang'				    => $r['role_pasang'],
                                                    'role_crossbranch'				=> $r['role_crossbranch']
											)
				);
			}
        }
		else
		{		
			array_push($data['data'], array( 'success' => "false" ));
		}  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }


	function getVersion_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $version  = $this->db->query("SELECT a.version FROM whversion_mobile a ORDER BY nomor DESC LIMIT 1")->row()->version;
        $url      = $this->db->query("SELECT a.url FROM whversion_mobile a ORDER BY nomor DESC LIMIT 1")->row()->url;
        
        array_push($data['data'], array( 
    									'version' 	=> $version, 
    									'url'	=> $url
        								)
        );

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
}
