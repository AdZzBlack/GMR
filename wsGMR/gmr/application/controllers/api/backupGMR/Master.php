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
class Master extends REST_Controller { 

    function __construct()
    {
        // Construct the parent class
        parent::__construct();

         // Configure limits on our controller methods
        // Ensure you have created the 'limits' table and enabled 'limits' within application/config/rest.php
        $this->methods['event_post']['limit'] = 50000; // 500 requests per hour per event/key
        // $this->methods['event_delete']['limit'] = 50; // 50 requests per hour per event/key
        $this->methods['event_get']['limit'] = 50000; // 500 requests per hour per event/key

        header("Access-Control-Allow-Origin: *");
        header("Access-Control-Allow-Methods: GET, POST");
        header("Access-Control-Allow-Headers: Origin, Content-Type, Accept, Authorization");
    }

    function clean($string) {
        return preg_replace("/[^[:alnum:][:space:]]/u", '', $string); // Replaces multiple hyphens with single one.
    }

    function error($string) {
        return str_replace( array("\t", "\n") , "", $string);
    }

    function alldatabangunan_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorMBangunan = (isset($jsonObject["nomor_bangunan"]) ? $this->clean($jsonObject["nomor_bangunan"]) : "");
        if($intNomorMBangunan != ""){ $intNomorMBangunan = " AND a.nomor = " . $intNomorMBangunan; }

		$intNomorMHeader = (isset($jsonObject["nomor_header"]) ? $this->clean($jsonObject["nomor_header"]) : "");
        if($intNomorMHeader != ""){ $intNomorMHeader = " AND a.nomorheader = " . $intNomorMHeader; }
		
		$needElevasi = (isset($jsonObject["need_elevasi"]) ? $this->clean($jsonObject["need_elevasi"]) : "");
		$checkElevasi = (isset($jsonObject["check_elevasi"]) ? $this->clean($jsonObject["check_elevasi"]) : "");
		
        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.namalengkap LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

		$query = "  SELECT  
                        a.nomor,
						a.nomorheader,
						a.kode,
						a.nama,
						a.keterangan,
						a.catatan,
						a.namalengkap,
						a.status_anak,
						a.tipe
                    FROM mhbangunan_view a
					WHERE 1 = 1 $intNomorMBangunan $intNomorMHeader $search $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
			$avaiable = 0;
            foreach ($result->result_array() as $r){
				$display = false;
				if($needElevasi == "1"  && $r['status_anak'] == "0")
				{
					$nomor_ba  = $this->db->query("SELECT a.nomor FROM mhba a WHERE nama = 'Berita Acara Elevasi' LIMIT 1")->row()->nomor;
					
					$countElevasi  = $this->db->query("SELECT COUNT(1) AS count_elevasi FROM thberitaacara a WHERE a.status_disetujui = 1 AND nomormhba = $nomor_ba AND a.nomormhbangunan =" . $r['nomor'])->row()->count_elevasi;
					if($countElevasi>0)
					{
						$avaiable++;
						$display = true;
					}
				}
				else if($checkElevasi == "1"  && $r['status_anak'] == "0")
				{
					$nomor_ba  = $this->db->query("SELECT a.nomor FROM mhba a WHERE nama = 'Berita Acara Elevasi' LIMIT 1")->row()->nomor;
					
					$countElevasi  = $this->db->query("SELECT COUNT(1) AS count_elevasi FROM thberitaacara a WHERE (a.status_disetujui = 1 OR a.status_disetujui = 0) AND nomormhba = $nomor_ba AND a.nomormhbangunan =" . $r['nomor'])->row()->count_elevasi;
					if($countElevasi==0)
					{
						$avaiable++;
						$display = true;
					}
				}
				else
				{
					$display = true;
				}
				
				if($display)
				{
					array_push($data['data'], array(
													'nomor'    			=> $r['nomor'], 
													'nomorheader'       => $r['nomorheader'], 
													'kode'              => $r['kode'],
													'nama'       		=> $r['nama'],
													'keterangan'        => $r['keterangan'],
													'catatan'           => $r['catatan'],
													'namalengkap'       => $r['namalengkap'],
													'status_anak'       => $r['status_anak'],
													'tipe'		        => $r['tipe'],
													)
					);
				}
            }
			
			if($needElevasi == "1" && $avaiable == 0)
			{
				array_push($data['data'], array( 'query' => $this->error("no data") ));
			}
        }else{      
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
	
	function alldatarab_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorMBangunan = (isset($jsonObject["nomor_bangunan"]) ? $this->clean($jsonObject["nomor_bangunan"]) : "2");
        if($intNomorMBangunan != ""){ $intNomorMBangunan = " AND a.nomormhbangunan = " . $intNomorMBangunan; }
		
        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.keterangan LIKE '%$search%') "; }

		$query = "  SELECT  
						a.nomor AS nomor,
                        b.nama AS nama
                    FROM mdrab a
					JOIN mhpekerjaan b ON a.nomormhpekerjaan = b.nomor
					WHERE 1 = 1 $intNomorMBangunan $search";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
				array_push($data['data'], array(
												'nomor'    			=> $r['nomor'], 
												'nama'       		=> $r['nama'],
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
	
	function alldatarabWithCheck_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorMBangunan = (isset($jsonObject["nomor_bangunan"]) ? $this->clean($jsonObject["nomor_bangunan"]) : "18");
        if($intNomorMBangunan != ""){ $intNomorMBangunan = " AND a.nomormhbangunan = " . $intNomorMBangunan; }
		
        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.keterangan LIKE '%$search%') "; }

		$query = "  SELECT  
						a.nomor AS nomor,
                        b.nama AS nama
                    FROM mdrab a
					JOIN mhpekerjaan b ON a.nomormhpekerjaan = b.nomor
					WHERE 1 = 1 
						AND a.progress < 100 $intNomorMBangunan $search";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
				$keterangan  = $this->db->query("select FC_CHECK_OPNAME_VALIDITY(".$r['nomor'].") AS keterangan")->row()->keterangan;
				
				array_push($data['data'], array(
												'nomor'    			=> $r['nomor'], 
												'nama'       		=> $r['nama'],
												'keterangan'       		=> $keterangan,
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
	
	function alldatadeliveryorder_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
        $intNomorMBangunan = (isset($jsonObject["nomor_bangunan"]) ? $this->clean($jsonObject["nomor_bangunan"]) : "18");
        if($intNomorMBangunan != ""){ $intNomorMBangunan = " AND a.nomormhbangunan = " . $intNomorMBangunan; }

		$query = "  SELECT  
						a.nomor AS nomor,
                        a.kode AS kode,
						a.dibuat_pada AS tanggal
                    FROM thdeliveryorder a
					WHERE 1 = 1 AND a.status_disetujui = 1 $intNomorMBangunan $search";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
				
				$query1 = "SELECT  
							a.nomor AS nomor,
							b.nama AS nama,
							a.jumlahorder - FC_GENERATE_SISA_KIRIM(a.nomor) AS jumlah,
							c.nama AS satuan,
							a.nomormhbarang AS nomorbarang,
							a.harga AS harga
							FROM tddeliveryorder a
							JOIN mhbarang b ON a.nomormhbarang = b.nomor
							JOIN mhsatuan c ON b.nomormhsatuan = c.nomor
							WHERE 1 = 1 
								AND a.jumlahorder - FC_GENERATE_SISA_KIRIM(a.nomor) > 0
								AND a.nomorthdeliveryorder = ".$r['nomor'];
				$result1 = $this->db->query($query1);
				
				$temp = array();
				$ada = false;
				if( $result1 && $result1->num_rows() > 0){
					$ada = true;
					foreach ($result1->result_array() as $r1){
						array_push($temp, array(
												'nomor'    			=> $r1['nomor'], 
												'nama'    			=> $r1['nama'], 
												'jumlah'       		=> $r1['jumlah'],
												'satuan'       		=> $r1['satuan'],
												'nomorbarang' 		=> $r1['nomorbarang'],
												'harga'       		=> $r1['harga']
												)
						);
					}
				}
				
				if($ada)
				{
					array_push($data['data'], array(
													'nomor'    			=> $r['nomor'], 
													'kode'       		=> $r['kode'],
													'tanggal'       	=> $r['tanggal'],
													'detail'       		=> $temp
													)
					);	
				}
            }
        }else{      
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
	
	// --- GET USER MOBILE DATA --- //
    function alldatausermessage_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

		$nomoruser = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"]) : "");
		
        $intNomorMUser = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"]) : "");
        if($intNomorMUser != ""){ $intNomorMUser = " AND a.nomor <> " . $intNomorMUser; }
		
        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.userid LIKE '%$search%') "; }

		$query = "  SELECT  
                        a.nomor,
						a.userid
                    FROM whuser_mobile a
					WHERE 1 = 1 $intNomorMUser $search";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
				$count_notif = $this->db->query("SELECT COUNT(1) as count_notif FROM whchat_mobile WHERE nomoruser_from = '". $r['nomor'] ."' AND nomoruser_to = '". $nomoruser ."' AND status_read = '1'")->row()->count_notif;
                array_push($data['data'], array(
												'nomor'	=> $r['nomor'],
												'user_nama' => $r['userid'],
												'notif' => $count_notif
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
	
	function getCount_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

		$user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"]) : "1");
		$user_id = (isset($jsonObject["user_id"]) ? $this->clean($jsonObject["user_id"]) : "1");
		
		$nomor_ba  = $this->db->query("SELECT a.nomor FROM mhba a WHERE nama = 'Berita Acara Elevasi' LIMIT 1")->row()->nomor;
		
		$elevasi_baru  = $this->db->query("SELECT COUNT(1) AS elevasi_baru FROM thberitaacara a WHERE a.status_disetujui = 0 AND nomormhba = $nomor_ba")->row()->elevasi_baru;
		$order_baru  = $this->db->query("SELECT COUNT(1) AS order_baru FROM thdeliveryorder a WHERE a.status_disetujui = 0")->row()->order_baru;
		$order_approved_baru  = $this->db->query("SELECT COUNT(1) AS order_approved_baru FROM thdeliveryorder a WHERE a.status_disetujui = 1 AND a.status_print = 0 AND a.dibuat_oleh = " . $user_nomor)->row()->order_approved_baru;
		$order_disapproved_baru  = $this->db->query("SELECT COUNT(1) AS order_disapproved_baru FROM thdeliveryorder a WHERE a.status_disetujui = 2 AND a.dibuat_oleh = " . $user_nomor)->row()->order_disapproved_baru;
		$private_message = $this->db->query("SELECT COUNT(1) AS private_message FROM whchat_mobile a WHERE a.nomoruser_to = ". $user_id ." AND a.status_read = '1'")->row()->private_message;
        $result = $this->db->query($query);

        array_push($data['data'], array(
										'elevasi_baru'    			=> $elevasi_baru,
										'order_baru'    			=> $order_baru,
										'order_approved_baru'    	=> $order_approved_baru,
										'order_disapproved_baru'    => $order_disapproved_baru,
										'private_message'			=> $private_message,
										)
		);

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
}
