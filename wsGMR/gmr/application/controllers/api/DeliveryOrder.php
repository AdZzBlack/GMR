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
class DeliveryOrder extends REST_Controller { 

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

    function alldatarabdetail_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $filterNomorMRAB = '';

        $intNomorMRAB = (isset($jsonObject["nomor_rab"]) ? $this->clean($jsonObject["nomor_rab"]) : "");
        if($intNomorMRAB != ""){ $filterNomorMRAB = " AND a.nomormdrab = " . $intNomorMRAB; }

        $intNomorBangunan = (isset($jsonObject["nomor_bangunan"]) ? $this->clean($jsonObject["nomor_bangunan"]) : "");

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (b.nama  LIKE '%$search%') "; }

//        $intNomorMRAB = (isset($jsonObject["nomor_rab"]) ? $this->clean($jsonObject["nomor_rab"]) : "");
//        if($intNomorMRAB != ""){ $intNomorMRAB = " AND a.nomormdrab = " . $intNomorMRAB; }
//
//        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
//        if($search != ""){ $search = " AND (b.nama  LIKE '%$search%') "; }

		/*$query = "  SELECT
						a.nomor AS nomor,
                        b.nomor AS nomorbarang,
						b.nama  AS namabarang,
						c.nama  AS satuan,
						CASE WHEN b.pengaruhelevasi=1 THEN
							a.jumlah+(ROUND(a.jumlah*(FC_GET_ELEVASI(d.nomormhbangunan)-3)/3,2))
							ELSE
							a.jumlah
							END as jumlah,
						CASE WHEN FC_GET_ELEVASI(d.nomormhbangunan)>3 AND b.pengaruhelevasi=1
							THEN CONCAT('Elevasi lebih ', (FC_GET_ELEVASI(d.nomormhbangunan)-3), ' cm')
							ELSE
							'' 
							END AS keterangan,
						f.harga AS harga,
						a.jumlahterorder AS do,
						a.perkiraanpersenwaste AS waste
                      FROM mdrabdetail a
					  JOIN mhbarang b ON a.nomormhbarang = b.nomor
					  JOIN mhsatuan c ON b.nomormhsatuan = c.nomor
					  JOIN mdrab d ON a.nomormdrab=d.nomor
					  JOIN mdpekerjaan f ON a.nomormhpekerjaan = f.nomormhpekerjaan AND b.nomor = f.nomormhbarang
					WHERE 1 = 1
						AND a.status_aktif = 1
						AND b.status_aktif = 1
						AND c.status_aktif = 1
						AND d.status_aktif = 1
						AND f.status_aktif = 1
						AND (b.prioritas<=IFNULL((
							SELECT MAX(d.prioritas)+1 FROM tdbpm a 
							JOIN tddeliveryorder b ON b.nomor=a.nomortddeliveryorder
							JOIN thdeliveryorder c ON c.nomor=b.nomorthdeliveryorder
							JOIN mhbarang d ON a.nomormhbarang=d.nomor
							WHERE a.status_aktif>0 AND a.jumlahkirim>0 AND c.nomormhbangunan=d.nomormhbangunan),1))
						$intNomorMRAB $search";
						*/

//      				'nomor'    		=> $r['nomor'],
//                      'nomorbarang'   => $r['nomorbarang'],
//                      'namabarang'    => $r['namabarang'],
//                      'satuan'       	=> $r['satuan'],
//                      'jumlah'       	=> $r['jumlah'],
//                      'harga'       	=> $r['harga'],
//                      'do'       		=> $r['do'],
//                      'waste'     	=> $r['waste'],
//                      'keterangan'	=> $r['keterangan'],

		$query = "SELECT a.nomormhbarang AS nomorbarang,
                    b.nama AS namabarang,
                    c.nama AS satuan,
                    e.nama as namapekerjaan,
                    '' AS keterangan,
                    a.harga,
                    a.nomor,
                    esr.jumlah AS volume_1,
                    d.volume AS volume_2,
                    (esr.jumlah * d.volume) AS jumlah_volume,
                  CASE WHEN b.pengaruhelevasi=1
                      THEN FC_GET_ELEVASI('$intNomorBangunan')
                      ELSE 0
                  END AS elevasi,
                  CASE WHEN b.pengaruhelevasi=1
                      THEN ((esr.jumlah * d.volume) * ((FC_GET_ELEVASI('$intNomorBangunan')-2)/3))
                      ELSE (esr.jumlah * d.volume)
                  END AS jumlah_elevasi,
                  a.perkiraanpersenwaste AS waste,
                  CASE WHEN b.pengaruhelevasi=1
                      THEN ROUND(((esr.jumlah * d.volume) * ((FC_GET_ELEVASI('$intNomorBangunan')-2)/3)) + (((esr.jumlah * d.volume) * ((FC_GET_ELEVASI('$intNomorBangunan')-2)/3)) * a.perkiraanpersenwaste/100),3)
                      ELSE ROUND((esr.jumlah * d.volume) + ((esr.jumlah * d.volume) * a.perkiraanpersenwaste/100),3)
                  END AS jumlah,
                  a.jumlahterorder AS do,
                  CASE WHEN b.pengaruhelevasi=1
                      THEN ((esr.jumlah * d.volume) * ((FC_GET_ELEVASI('$intNomorBangunan')-2)/3)) + (((esr.jumlah * d.volume) * ((FC_GET_ELEVASI('$intNomorBangunan')-2)/3)) * a.perkiraanpersenwaste/100) - a.jumlahterorder
                      ELSE (esr.jumlah * d.volume) + ((esr.jumlah * d.volume) * a.perkiraanpersenwaste/100) - a.jumlahterorder
                  END AS sisa,
                  FC_CHECK_BARANG_VALIDITY(a.nomormhbarang,'$intNomorBangunan'),
                  FC_CHECK_OPNAME_VALIDITY(d.nomor)
                  FROM mdrabdetail a
                  JOIN mhbarang b ON a.nomormhbarang=b.nomor
                  JOIN mhsatuan c ON b.nomormhsatuan=c.nomor
                  JOIN mdrab d ON a.nomormdrab=d.nomor AND d.nomor NOT IN (
                      SELECT nomormdrab from thopname where progress=100
                  )
                  JOIN mhpekerjaan e ON d.nomormhpekerjaan=e.nomor
                      JOIN mhsatuan es ON e.nomormhsatuan=es.nomor AND es.adarumus=1
                      JOIN mdsatuan_rumus esr ON esr.nomormhsatuan = es.nomor AND esr.nomormhbarang = b.nomor
                  WHERE a.status_aktif > 0 AND (b.prioritas<=IFNULL((
                      SELECT MAX(d.prioritas)+1 FROM tdbpm a
                      JOIN tddeliveryorder b ON b.nomor=a.nomortddeliveryorder
                      JOIN thdeliveryorder c ON c.nomor=b.nomorthdeliveryorder
                      JOIN mhbarang d ON a.nomormhbarang=d.nomor
                      WHERE a.status_aktif>0 AND a.jumlahkirim>0 AND c.nomormhbangunan='$intNomorBangunan'),1))
                  AND FC_CHECK_BARANG_VALIDITY(a.nomormhbarang,'$intNomorBangunan')='1'
                  AND FC_CHECK_OPNAME_VALIDITY(d.nomor)='1'
                  AND d.status_aktif=1
                  AND d.nomormhbangunan='$intNomorBangunan'
                  $filterNomorMRAB $search  ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
				array_push($data['data'], array(
												'nomor'    		=> $r['nomor'], 
												'nomorbarang'   => $r['nomorbarang'],
												'namabarang'    => $r['namabarang'],
												'satuan'       	=> $r['satuan'],
												'jumlah'       	=> $r['jumlah'],
												'harga'       	=> $r['harga'],
												'do'       		=> $r['do'],
												'waste'     	=> $r['waste'],
												'keterangan'	=> $r['keterangan'],
												'querys'        => $query
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
	
	function createDeliveryOrder_post(){     
		$data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$userNomor          = (isset($jsonObject["userNomor"])  	? $jsonObject["userNomor"]                     : "");
		$tanggal            = (isset($jsonObject["tanggal"]) 		? $jsonObject["tanggal"]                       : "");
		$bangunan_nomor     = (isset($jsonObject["nomor_bangunan"]) ? $jsonObject["nomor_bangunan"]                : "");
		$dataDO             = (isset($jsonObject["dataDO"])  		? $jsonObject["dataDO"]                        : "");
		
		$this->db->trans_begin();
		
		$query = "INSERT INTO thdeliveryorder(`nomormhbangunan`, `dibuat_pada`, `dibuat_oleh`, `kode`) VALUES($bangunan_nomor, '$tanggal', $userNomor, FC_GENERATE_DELIVERY_ORDER_KODE())";
		
        $this->db->query($query);

        $header_nomor = $this->db->insert_id();
		
		$approve = 1;
		
		if($dataDO != "")
		{
			$pieces = explode("|", $dataDO);
			foreach ($pieces as $arr) {
                $valuedata = explode("~", $arr);

                if( $valuedata[0] != ""){
					
					if($valuedata[5]==0) $approve = 0;
					
					$catatan = "";
					if($valuedata[7]!="0") $catatan = $valuedata[7];
					
                    $query_detail_do = $this->db->insert_string('tddeliveryorder', array(
                                                                          'nomorthdeliveryorder'=>$header_nomor,
																		  'nomormdrabdetail'	=>$valuedata[6], 
                                                                          'nomormhbarang'     	=>$valuedata[0], 
                                                                          'jumlahorder' 		=>$valuedata[3], 
                                                                          'harga' 				=>$valuedata[4],
																		  'status_disetujui'	=>$valuedata[5],
																		  'dibuat_oleh'			=>$userNomor,
																		  'keterangan'			=>$catatan
                                                                        )
                                                    );
                    $this->db->query($query_detail_do);
					
                }
            }
		}
		
		
		if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'failed' => $query ));
        }else{
            $this->db->trans_commit();
			
			$kode = $this->db->query("SELECT kode FROM thdeliveryorder where nomor = $header_nomor")->row()->kode;
			$tanggal = $this->db->query("SELECT dibuat_pada FROM thdeliveryorder where nomor = $header_nomor")->row()->dibuat_pada;
			$bangunan = $this->db->query("SELECT b.nama FROM thdeliveryorder a JOIN mhbangunan_view b ON a.nomormhbangunan = b.nomor where a.nomor = $header_nomor")->row()->nama;
			$nomor_project = $this->db->query("SELECT b.nomorproject FROM thdeliveryorder a JOIN mhbangunan_view b ON a.nomormhbangunan = b.nomor where a.nomor = $header_nomor")->row()->nomorproject;
			$project = $this->db->query("SELECT nama FROM mhbangunan WHERE nomor = $nomor_project")->row()->nama;
			
            array_push($data['data'], array( 
										'nomor'	=> $header_nomor,
										'success' => 'true',
										'kode' => $kode,
										'tanggal' => $tanggal,
										'bangunan' => $bangunan,
										'project' => $project,
								));
			
			if($approve==0)
			{
				$regisID = array();
				
				$query_getuser = " SELECT 
									a.gcm_id AS gcmid
									FROM mhadmin a
									JOIN whrole_mobile b ON a.role_android = b.nomor
									WHERE a.status_aktif > 0 AND (a.gcm_id <> '' AND a.gcm_id IS NOT NULL) AND b.approvedeliveryorder = 1 ";
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
			
			$query1 = "CALL SP_CHECK_DELIVERY_ORDER($header_nomor, 0, 0)";
			$this->db->query($query1);
        } 
		
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
	}
	
	function editDeliveryOrder_post(){     
		$data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$userNomor          = (isset($jsonObject["userNomor"])  	? $jsonObject["userNomor"]                     : "");
		$headerNomor       = (isset($jsonObject["headerNomor"])   ? $jsonObject["headerNomor"]                  : "");
		$dataDO             = (isset($jsonObject["dataDO"])  		? $jsonObject["dataDO"]                        : "");
		
		$this->db->trans_begin();
		
		$approve = 1;
		
		if($dataDO != "")
		{
			$pieces = explode("|", $dataDO);
			foreach ($pieces as $arr) {
                $valuedata = explode("~", $arr);

                if( $valuedata[0] != ""){
					
					if($valuedata[3]==0) $approve = 0;
					
					$catatan = "";
					if($valuedata[2]!="0") $catatan = $valuedata[2];
					
					$query = "UPDATE tddeliveryorder SET 
								jumlahorder = ".$valuedata[1].",
								keterangan = '".$catatan."',
								status_disetujui = ".$valuedata[3]."
								WHERE 1 = 1 AND nomor = " . $valuedata[0];
					$this->db->query($query);
                }
            }
		}
		
		if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'failed' => $query ));
        }else{
            $this->db->trans_commit();
			
			$query = "SELECT kode FROM thdeliveryorder where nomor = $headerNomor";
			$result = $this->db->query($query)->row()->kode;
			
            array_push($data['data'], array( 
										'nomor'	=> $headerNomor,
										'success' => $this->clean($result)
								));
			
			if($approve==0)
			{
				$regisID = array();
				
				$query_getuser = " SELECT 
									a.gcm_id AS gcmid
									FROM mhadmin a
									JOIN whrole_mobile b ON a.role_android = b.nomor
									WHERE a.status_aktif > 0 AND (a.gcm_id <> '' AND a.gcm_id IS NOT NULL) AND b.approvedeliveryorder = 1 ";
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
			
			$query1 = "CALL SP_CHECK_TDDELIVERYORDER($header_nomor, 0, 0)";
			$this->db->query($query1);
        } 
		
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
	}
	
	function alldataneedprint_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.kode LIKE '%$search%') "; }

        $cabang = (isset($jsonObject["cabang"]) ? $this->clean($jsonObject["cabang"]) : "");
        if($cabang != ""){ $cabang = " AND b.nomormhcabang = " . $cabang; }

		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (a.nomor = '$nomor') "; }
		
		$user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"]) : "3");
        if($user_nomor != ""){ $user_nomor = " AND (a.dibuat_oleh = '$user_nomor') "; }
		
        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

		$nomor_project = (isset($jsonObject["nomor_project"]) ? $this->clean($jsonObject["nomor_project"]) : "");
		if($nomor_project != ""){ $nomor_project = " AND (b.nomorproject = '$nomor_project') "; }
		
		$query = "  SELECT  
                        a.nomor,
						a.kode,
						a.status_print
					FROM thdeliveryorder a
					JOIN mhbangunan_view b ON a.nomormhbangunan = b.nomor $cabang
					WHERE 1 = 1
					AND a.status_aktif = 1
					AND a.status_disetujui = 1 $user_nomor $nomor_project $search $nomor $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'nomor'    			=> $r['nomor'], 
												'nama'       		=> $r['kode'],
												'print'       		=> $r['status_print'],
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
	
	function alldatadetailneedprint_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "12");
		$header_nomor = $nomor;
        if($nomor != ""){ $nomor = " AND (c.nomor = '$nomor') "; }

		$query = "  SELECT  
						e.nama AS item,
						a.jumlahorder AS jumlahorder,
						a.keterangan AS catatan,
						f.nama AS satuan
					FROM tddeliveryorder a
					JOIN mdrabdetail b ON a.nomormdrabdetail = b.nomor
					JOIN thdeliveryorder c ON a.nomorthdeliveryorder = c.nomor
					JOIN mhbangunan_view d ON c.nomormhbangunan = d.nomor
					JOIN mhbarang e ON a.nomormhbarang = e.nomor
					JOIN mhsatuan f ON e.nomormhsatuan = f.nomor
					WHERE 1 = 1 $nomor";
        $result = $this->db->query($query);

		$detail = "";
        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
				$catatan = "0";
				if($r['catatan']!="") $catatan = $r['catatan'];
				
				
				
                $detail = $detail . "0~" . $r['item'] . "~" . (float) $r['jumlahorder'] . " " . $r['satuan'] . "~0~0~0~0~" . $catatan . "|";
				
				/*
				array_push($data['data'], array(
												'item'    				=> $r['item'], 
												'jumlahorder' 			=> $r['jumlahorder'],
												'catatan'    			=> $r['catatan'], 
												'satuan'  				=> $r['satuan']
                                                )
                );
				*/
            }
			
			$kode = $this->db->query("SELECT kode FROM thdeliveryorder where nomor = $header_nomor")->row()->kode;
			$tanggal = $this->db->query("SELECT dibuat_pada FROM thdeliveryorder where nomor = $header_nomor")->row()->dibuat_pada;
			$bangunan = $this->db->query("SELECT b.nama FROM thdeliveryorder a JOIN mhbangunan_view b ON a.nomormhbangunan = b.nomor where a.nomor = $header_nomor")->row()->nama;
			$nomor_project = $this->db->query("SELECT b.nomorproject FROM thdeliveryorder a JOIN mhbangunan_view b ON a.nomormhbangunan = b.nomor where a.nomor = $header_nomor")->row()->nomorproject;
			$project = $this->db->query("SELECT nama FROM mhbangunan WHERE nomor = $nomor_project")->row()->nama;
			
            array_push($data['data'], array( 
										'nomor'	=> $header_nomor,
										'success' => 'true',
										'kode' => $kode,
										'tanggal' => $tanggal,
										'bangunan' => $bangunan,
										'project' => $project,
										'detail' => $detail
								));
        }else{      
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
	
	function alldataneededit_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.kode LIKE '%$search%') "; }

		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (a.nomor = '$nomor') "; }
		
		$user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"]) : "1");
        if($user_nomor != ""){ $user_nomor = " AND (a.dibuat_oleh = '$user_nomor') "; }
		
        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

		$query = "  SELECT  
                        a.nomor,
						a.kode
					FROM thdeliveryorder a
					WHERE 1 = 1
					AND a.status_disetujui = 2 $user_nomor $search $nomor $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'nomor'    			=> $r['nomor'], 
												'nama'       		=> $r['kode']
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
	
	function alldatadetailneededit_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (c.nomor = '$nomor') "; }

		$query = "  SELECT  
                        a.nomor AS nomor,
						e.nama AS item,
						a.nomorthdeliveryorder AS nomorthdeliveryorder,
						b.perkiraanpersenwaste AS waste,
						b.jumlah AS jumlah,
						b.jumlahterorder AS do,
						a.jumlahorder AS jumlahorder,
						a.keterangan AS catatan,
						d.namalengkap AS namalengkap,
						f.nama AS satuan
					FROM tddeliveryorder a
					JOIN mdrabdetail b ON a.nomormdrabdetail = b.nomor
					JOIN thdeliveryorder c ON a.nomorthdeliveryorder = c.nomor
					JOIN mhbangunan_view d ON c.nomormhbangunan = d.nomor
					JOIN mhbarang e ON a.nomormhbarang = e.nomor
					JOIN mhsatuan f ON e.nomormhsatuan = f.nomor
					WHERE 1 = 1
					AND a.status_disetujui = 2 $nomor";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'nomor'    				=> $r['nomor'], 
												'item'    				=> $r['item'], 
												'nomorthdeliveryorder'  => $r['nomorthdeliveryorder'],
												'waste'    				=> $r['waste'], 
												'jumlah'  				=> $r['jumlah'],
												'do'    				=> $r['do'], 
												'jumlahorder' 			=> $r['jumlahorder'],
												'catatan'    			=> $r['catatan'], 
												'namalengkap'  			=> $r['namalengkap'],
												'satuan'  				=> $r['satuan']
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
	
	function alldataneedapproval_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.kode LIKE '%$search%') "; }

        $cabang = (isset($jsonObject["cabang"]) ? $this->clean($jsonObject["cabang"]) : "");
        if($cabang != ""){ $cabang = " AND a.nomormhcabang = " . $cabang; }

		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (a.nomor = '$nomor') "; }
		
        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

		$query = "  SELECT  
                        a.nomor,
						a.kode
					FROM thdeliveryorder a
					JOIN mhbangunan_view b ON a.nomormhbangunan = b.nomor $cabang
					WHERE 1 = 1
					AND a.status_aktif = 1
					AND b.status_aktif = 1
					AND a.status_disetujui = 0 $search $nomor $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'nomor'    			=> $r['nomor'], 
												'nama'       		=> $r['kode']
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
	
	function alldatadetailneedapproval_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (c.nomor = '$nomor') "; }

		$query = "  SELECT  
                        a.nomor AS nomor,
						e.nama AS item,
						a.nomorthdeliveryorder AS nomorthdeliveryorder,
						b.perkiraanpersenwaste AS waste,
						b.jumlah AS jumlah,
						b.jumlahterorder AS do,
						a.jumlahorder AS jumlahorder,
						a.keterangan AS catatan,
						d.namalengkap AS namalengkap,
						f.nama AS satuan
					FROM tddeliveryorder a
					JOIN mdrabdetail b ON a.nomormdrabdetail = b.nomor
					JOIN thdeliveryorder c ON a.nomorthdeliveryorder = c.nomor
					JOIN mhbangunan_view d ON c.nomormhbangunan = d.nomor
					JOIN mhbarang e ON a.nomormhbarang = e.nomor
					JOIN mhsatuan f ON e.nomormhsatuan = f.nomor
					WHERE 1 = 1
					AND a.status_disetujui = 0 $nomor";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'nomor'    				=> $r['nomor'], 
												'item'    				=> $r['item'], 
												'nomorthdeliveryorder'  => $r['nomorthdeliveryorder'],
												'waste'    				=> $r['waste'], 
												'jumlah'  				=> $r['jumlah'],
												'do'    				=> $r['do'], 
												'jumlahorder' 			=> $r['jumlahorder'],
												'catatan'    			=> $r['catatan'], 
												'namalengkap'  			=> $r['namalengkap'],
												'satuan'  				=> $r['satuan']
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
	
	function print_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomorth = (isset($jsonObject["nomorth"]) ? $this->clean($jsonObject["nomorth"]) : "");

		$this->db->trans_begin();
		
		$query = "  UPDATE thdeliveryorder
                    SET status_print = 1
					WHERE 1 = 1 AND nomor= " . $nomorth;
        $this->db->query($query);
		
        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => $query ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => 'true' ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
	
	function approveall_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomorth = (isset($jsonObject["nomorth"]) ? $this->clean($jsonObject["nomorth"]) : "");

		$this->db->trans_begin();
		
		$query = "  UPDATE tddeliveryorder
                    SET status_disetujui = 1
					WHERE 1 = 1 AND nomorthdeliveryorder = " . $nomorth;
        $this->db->query($query);
		
		$vcGCMId = $this->db->query("SELECT b.gcm_id AS gcmid FROM thdeliveryorder a JOIN mhadmin b ON b.nomor = a.dibuat_oleh WHERE a.nomor = $nomorth")->row()->gcmid;
		
        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => $query1 ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => 'true' ));
			$query1 = "CALL SP_CHECK_TDDELIVERYORDER($nomorth, 0, 0)";
			$this->db->query($query1);
			
			$this->send_gcm($vcGCMId, $this->ellipsis('Delivery Order'),'Delivery Order Approved','DeliveryOrderApproved','0','0');
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
	
	function approveselected_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomorth = (isset($jsonObject["nomorth"]) ? $this->clean($jsonObject["nomorth"]) : "");
		$list = (isset($jsonObject["list"]) ? $jsonObject["list"] : "");

		$this->db->trans_begin();
		
		if($list != "")
		{
			$pieces = explode("|", $list);
			foreach ($pieces as $arr) {
				if($arr!="")
				{
					$query = "  UPDATE tddeliveryorder
								SET status_disetujui = 1
								WHERE 1 = 1 AND nomor = " . $arr;
					$this->db->query($query);
				}
			}
		}
		
		$query1 = "  UPDATE tddeliveryorder
                    SET status_disetujui = 2
					WHERE 1 = 1 
						AND status_disetujui = 0
						AND nomorthdeliveryorder = " . $nomorth;
        $this->db->query($query1);
		
		$query = "  UPDATE thdeliveryorder
                    SET status_disetujui = 2
					WHERE 1 = 1 AND nomor = " . $nomorth;
        $this->db->query($query);
		
		$vcGCMId = $this->db->query("SELECT b.gcm_id AS gcmid FROM thdeliveryorder a JOIN mhadmin b ON b.nomor = a.dibuat_oleh WHERE a.nomor = $nomorth")->row()->gcmid;
		
        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => $query ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => 'true' ));
			$this->db->query($query1);
			
			$this->send_gcm($vcGCMId, $this->ellipsis('Delivery Order'),'There is a disapprove delivery order','DeliveryOrderDispproved','0','0');
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
	
	function disapproveall_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomorth = (isset($jsonObject["nomorth"]) ? $this->clean($jsonObject["nomorth"]) : "");

		$this->db->trans_begin();
		
		$query = "  UPDATE tddeliveryorder
                    SET status_disetujui = 2
					WHERE 1 = 1 AND status_disetujui = 0 AND nomorthdeliveryorder = " . $nomorth;
        $this->db->query($query);
		
		$query = "  UPDATE thdeliveryorder
                    SET status_disetujui = 2
					WHERE 1 = 1 AND nomor = " . $nomorth;
        $this->db->query($query);
		
		$vcGCMId = $this->db->query("SELECT b.gcm_id AS gcmid FROM thdeliveryorder a JOIN mhadmin b ON b.nomor = a.dibuat_oleh WHERE a.nomor = $nomorth")->row()->gcmid;
		
        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => $query ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));
			
			$this->send_gcm($vcGCMId, $this->ellipsis('Delivery Order'),'Delivery order disapprove','DeliveryOrderDispproved','0','0');
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
}
