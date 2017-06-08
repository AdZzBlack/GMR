package com.adzzblack.gmr;

/**
 * Created by ADI on 06/04/2017.
 */
public class ItemAdapter {

    private String nomor;
    private String nama;

    public ItemAdapter(String nomor, String nama) {
        this.setNomor(nomor);
        this.setNama(nama);
    }

    public String getNomor() {
        return nomor;
    }

    public void setNomor(String nomor) {
        this.nomor = nomor;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

}
