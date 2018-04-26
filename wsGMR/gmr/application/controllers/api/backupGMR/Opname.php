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
class Opname extends REST_Controller { 

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
                    a.gcmid
                    FROM whuser_mobile a 
                    WHERE a.status_aktif > 0 AND (a.gcmid <> '' AND a.gcmid IS NOT NULL) AND a.nomor = $user_nomor ";
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
	
	function createOpname_post(){     
		$data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomor_user          = (isset($jsonObject["nomor_user"])  	? $jsonObject["nomor_user"]                    : "");
		$nomor_rab           = (isset($jsonObject["nomor_rab"])  	? $jsonObject["nomor_rab"]                     : "");
		$nomor_bangunan      = (isset($jsonObject["nomor_bangunan"])? $jsonObject["nomor_bangunan"]                : "");
		$progress            = (isset($jsonObject["progress"]) 	    ? $jsonObject["progress"]                      : "");
		$tanggal             = (isset($jsonObject["tanggal"]) 	    ? $jsonObject["tanggal"]                       : "");
		$photo 	             = (isset($jsonObject["photo"]) 	    ? $jsonObject["photo"]                         : "");
		  
		$this->db->trans_begin();
		
		$query = "INSERT INTO thopname(`nomormhbangunan`, `nomormdrab`, `progress`, `dibuat_pada`, `dibuat_oleh`, `photo`) VALUES($nomor_bangunan, $nomor_rab, $progress, '$tanggal', $nomor_user, $photo)";
	
		$this->db->query($query);
	
		$query1 = "UPDATE mdrab set progress = $progress WHERE nomor = $nomor_rab" ;
	
		$this->db->query($query1);
		
		
		if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'failed' => $query ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => 'success'));
        } 
		
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
	}
	
	function getRabDetail_post(){     
		$data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomor_rab          = (isset($jsonObject["nomor_rab"])  	? $jsonObject["nomor_rab"]                    : "1");
		if($nomor_rab != ""){ $nomor_rab = " AND a.nomor = " . $nomor_rab; }

		$query = "  SELECT  
						b.nama AS mandor,
						a.volume AS volume,
						c.nama AS satuan,
						a.progress AS progress
                    FROM mdrab a
					JOIN mhmandor b ON a.nomormhmandor = b.nomor
					JOIN mhsatuan c ON a.nomormhsatuan = c.nomor
					WHERE 1 = 1 $nomor_rab";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
				array_push($data['data'], array(
												'mandor'    		=> $r['mandor'], 
												'volume'       		=> $r['volume'],
												'satuan'       		=> $r['satuan'],
												'progress'     		=> $r['progress'],
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
	
	function createCreditNote_post(){     
		$data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		  
		$nomor_user          = (isset($jsonObject["nomor_user"])  	? $jsonObject["nomor_user"]                    : "1");
		$tanggal          	 = (isset($jsonObject["tanggal"])  		? $jsonObject["tanggal"]                       : date("Y-m-d h:i:s"));
		$tanggal_          	 = (isset($jsonObject["hanyatanggal"])	? $jsonObject["hanyatanggal"]                  : date("Y-m-d"));
		  
		$this->db->trans_begin();
		
		$cabang  = $this->db->query("SELECT nomormhcabang FROM mhadmin WHERE nomor = $nomor_user")->row()->nomormhcabang;
		$kode = 'kode';
		
		$query = "  SELECT
						c.nomormhmandor AS mandor,
						a.nomormhbangunan AS bangunan
					FROM thopname a
					JOIN mdrab c ON a.nomormdrab = c.nomor
					WHERE a.status_notabeli = 0
					GROUP BY mandor, bangunan";
        $result = $this->db->query($query);
		
		if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
				$kode  = $this->db->query("SELECT FC_GENERATE_THNOTABELI_KODE() AS kode")->row()->kode;
				
				$query_insert = $this->db->insert_string('thnotabeli', array(
															  'nomormhcabang'		=>$cabang,
															  'nomormhprincipal'	=>$r['mandor'], 
															  'nomormhbangunan'		=>$r['bangunan'], 
															  'tipe_principal'     	=>'mandor', 
															  'nomormhcurrency' 	=>1, 
															  'kode' 				=>$kode,
															  'kurs'				=>1,
															  'tanggal'				=>$tanggal_,
															  'dibuat_oleh'			=>$nomor_user,
															  'dibuat_pada'			=>$tanggal
															)
										);
				$this->db->query($query_insert);
				
				$header_nomor = $this->db->insert_id();
				
				$query1 = "  SELECT
								a.nomor AS nomor,
								d.nama AS nama,
								(a.progress - (SELECT IFNULL(SUM(b.progress),0) FROM thopname b WHERE b.nomor < a.nomor AND b.nomormdrab = a.nomormdrab))/100 AS qty,
								c.subtotal AS harga,
								(a.progress - (SELECT IFNULL(SUM(b.progress),0) FROM thopname b WHERE b.nomor < a.nomor AND b.nomormdrab = a.nomormdrab))/100 * c.subtotal AS subtotal
							FROM thopname a
							JOIN mdrab c ON a.nomormdrab = c.nomor
							JOIN mhpekerjaan d ON c.nomormhpekerjaan = d.nomor
							WHERE a.status_notabeli = 0
								AND c.nomormhmandor = " . $r['mandor'];
				$result1 = $this->db->query($query1);
				
				$total = 0;
				
				if( $result1 && $result1->num_rows() > 0){
					foreach ($result1->result_array() as $r1){
						$total += $r1['subtotal'];
						$query_insert1 = $this->db->insert_string('tdnotabeli', array(
																	  'nomorthnotabeli'		=>$header_nomor,
																	  'nomorthopname'		=>$r1['nomor'], 
																	  'nomormhcurrency'		=>1,
																	  'kode'				=>$kode,
																	  'nama'				=>$r1['nama'], 
																	  'qty'					=>$r1['qty'], 
																	  'kurs'				=>1, 
																	  'harga'				=>$r1['harga'], 
																	  'subtotal'			=>$r1['subtotal'],
																	  'dibuat_oleh'			=>$nomor_user,
																	  'dibuat_pada'			=>$tanggal
																	)
												);
						$this->db->query($query_insert1);
						
						$query2 = "UPDATE thopname SET status_notabeli = 1 WHERE nomor = " . $r1['nomor'];
						$this->db->query($query2);
					}
				}
				
				$query1 = "UPDATE thnotabeli SET subtotal = " . $total . " WHERE nomor = $header_nomor";
				$this->db->query($query1);
				
				$query_insert2 = $this->db->insert_string('tlaporanhutang', array(
																	  'intNomorMSupplier'	=>$r['mandor'],
																	  'intNomorMValuta'		=>1,
																	  'vcJenis'				=>'NK', 
																	  'vcJenisPrincipal'	=>'mandor',
																	  'intNomorTransaksi'	=>$header_nomor,
																	  'nomormhbangunan'		=>$r['bangunan'], 
																	  'intNomorMCabang'		=>$cabang, 
																	  'vcKodeTransaksi'		=>$kode, 
																	  'dtTanggalTransaksi'	=>$tanggal, 
																	  'decKurs'				=>1,
																	  'decTotal'			=>$total,
																	  'dtTanggal'			=>$tanggal_
																	)
														);
				$this->db->query($query_insert2);
				
            }
        }
		
		if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'failed' => $query_insert ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => 'success'));
        } 
		
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
	}
	
	function getOpname_post(){     
		$data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$query = "  SELECT
						a.nomor AS nomoropname,
						b.namalengkap AS namalengkap,
						d.nama AS mandor,
						a.progress AS progress,
						e.nama AS pekerjaan,
						a.dibuat_pada AS tanggal
					FROM thopname a
					JOIN mhbangunan_view b ON a.nomormhbangunan = b.nomor
					JOIN mdrab c ON a.nomormdrab = c.nomor
					JOIN mhmandor d ON c.nomormhmandor = d.nomor
					JOIN mhpekerjaan e ON c.nomormhpekerjaan = e.nomor
					WHERE a.status_notabeli = 0";
        $result = $this->db->query($query);
		
		if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
				array_push($data['data'], array(
												'nomoropname'    	=> $r['nomoropname'], 
												'namalengkap'  		=> $r['namalengkap'],
												'mandor'       		=> $r['mandor'],
												'progress'     		=> $r['progress'],
												'pekerjaan'    		=> $r['pekerjaan'],
												'tanggal'     		=> $r['tanggal']
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
	
	function editProgress_post(){     
		$data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomor          = (isset($jsonObject["nomor"])  	? $jsonObject["nomor"]                    : "");
		$input          = (isset($jsonObject["input"])  	? $jsonObject["input"]                    : "");
		
		$this->db->trans_begin();
		
		$query = "UPDATE thopname SET progress = $input WHERE nomor = $nomor";
        $this->db->query($query);
		
		if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'query' => $query ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => 'success'));
        } 
		
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
	}
}
