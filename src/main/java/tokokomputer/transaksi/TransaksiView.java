/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tokokomputer.transaksi;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import tokokomputer.barang.CariBarangView;
import tokokomputer.Pengaturan.Koneksi;

/**
 *
 * @author Humaidi
 */
public class TransaksiView extends javax.swing.JInternalFrame {

    /**
     * Creates new form TransaksiView
     */
    public TransaksiView() {
        initComponents();
        data_pelanggan();
        ulang();
    }
    
    PreparedStatement pst;
    ResultSet rs;
    Connection conn = new Koneksi().getKoneksi();
    String sql;
    DefaultTableModel dtm;

    private void nota_otomatis() {
        try {
            //query mengambil kode barang terakhir
            sql = "select no_nota from tb_penjualan order by no_nota desc limit 1";
            pst = conn.prepareStatement(sql);//menjalankan query sql
            rs = pst.executeQuery();//query select ,maka ambildatanya menggunakan rs/resultSet
            if (rs.next()) {//jika data dari query di atas ada
                //ambil kode barang terakhir.kemudian substring(4):ambilhuruf dari index ke 4 lalu tambahkan 1
                int kode = Integer.parseInt(rs.getString(1).substring(4)) + 1;
                textNota.setText("NTA-"+kode);//tampilkan kode barang dengan format: BRG-(nilai kode)   
            } else {
                //apabila data barang kosong,maka kode barang pertama adalah BRG-1000
                textNota.setText("NTA-1000");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.toString());
        }
    }

    private void data_pelanggan() {
        try {
            comboPelanggan.removeAllItems();//menghapus seluruh item di comboPelanggan
            comboPelanggan.addItem("Pilih Pelanggan");//menambahkan 1 item di comboPelanggan
            pst = conn.prepareStatement("select nama_pelanggan from tb_pelanggan");//menjalankan query
            rs = pst.executeQuery();//ambil data menggunakan rs/Resultset
            while (rs.next()) {
                comboPelanggan.addItem(rs.getString(1));//menambahkanitem pada comboPelanggan
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.toString());
        }
    }

    private void ulang() {
        nota_otomatis();//panggil method nota otomatis
        comboPelanggan.setSelectedIndex(0);//atur posisi item pada combobox
        textNota.setEnabled(false);
        textIDPelanggan.setEnabled(false);
        textKodeBarang.setEnabled(false);
        textNamaBarang.setEnabled(false);
        textKategori.setEnabled(false);
        textHarga.setEnabled(false);
        textStok.setEnabled(false);
        textTotal.setEnabled(false);
        textIDPelanggan.setText("");
        textKodeBarang.setText("");
        textNamaBarang.setText("");
        textKategori.setText("");
        textHarga.setText("");
        textStok.setText("");
        textQty.setText("");
        textTotal.setText("");
        textBayar.setText("");
        textKembali.setText("");
        textKembali.setEnabled(false);
        dtm = (DefaultTableModel) tabelItemBelanja.getModel();//baris 87 - 91 digunakan untuk menghapus item pada tabel 
        while (dtm.getRowCount() > 0) {
            dtm.removeRow(0);
        }
    }

    private void hitung_total() {
        BigDecimal total = new BigDecimal(0);
        for (int a = 0; a < tabelItemBelanja.getRowCount(); a++) {
            total = total.add(new BigDecimal(tabelItemBelanja.getValueAt(a, 5).toString()));
        }
        textTotal.setText(total.toString());
    }

    private boolean validasi() {
        boolean cek = false;
        java.util.Date tgl = textTanggal.getDate();//import java.util.data apabila erorr
        if (tgl == null) {
            JOptionPane.showMessageDialog(null, "Tanggal Transaksi belim diisi", null, JOptionPane.ERROR_MESSAGE);
            textTanggal.requestFocus();
        } else if (textIDPelanggan.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Data pelanggan belum di pilih", null, JOptionPane.ERROR_MESSAGE);
            comboPelanggan.requestFocus();
        } else if (tabelItemBelanja.getRowCount() <= 0) {
            JOptionPane.showMessageDialog(null, "Data barang belanja masih kosong", null, JOptionPane.ERROR_MESSAGE);
            buttonCariBarang.requestFocus();
        } else if (textBayar.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Textbox bayar belum diisi", null, JOptionPane.ERROR_MESSAGE);
            textBayar.requestFocus();
        } else if (Integer.parseInt(textBayar.getText()) < Integer.parseInt(textTotal.getText())) {
            JOptionPane.showMessageDialog(null, "Tidak Melayni Hutang!", null, JOptionPane.ERROR_MESSAGE);
            textBayar.requestFocus();
        } else {
            cek = true;
        }
        return cek;
    }

    private void simpan_transaksi() {
        if (validasi()) {//jika method validasi bernilai true
            try {
                java.util.Date d = textTanggal.getDate();
                java.sql.Date tgl = new java.sql.Date(d.getTime());//mengatur format tanggal 
                pst = conn.prepareStatement("insert into tb_penjualan values(?,?,?,?,?,?)");
                pst.setString(1, textNota.getText());
                pst.setString(2, tgl.toString());
                pst.setString(3, textIDPelanggan.getText());
                pst.setBigDecimal(4, new BigDecimal(textTotal.getText()));
                pst.setBigDecimal(5, new BigDecimal(textBayar.getText()));
                pst.setBigDecimal(6, new BigDecimal(textKembali.getText()));
                int isSucces = pst.executeUpdate();
                if (isSucces == 1) {//jika simpan berhasil
                    simpan_item_belanja();//
                }
                JOptionPane.showMessageDialog(null, "Data berhasil Disimpan");
                ulang();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Terjadi kesalahan pada Simpan Transaksi Detail \n" + ex.toString());
            }
        }
    }

    private void simpan_item_belanja() {
        for (int a = 0; a <= tabelItemBelanja.getRowCount() - 1; a++) {
            try {
                pst = conn.prepareStatement("insert into tb_detail_penjualan(no_nota,kode_barang,qty)values (?,?,?)");
                String kode;
                int jumlah;
                kode = tabelItemBelanja.getValueAt(a, 0).toString();
                jumlah = Integer.parseInt(tabelItemBelanja.getValueAt(a, 4).toString());
                pst.setString(1, textNota.getText());
                pst.setString(2, kode);
                pst.setInt(3, jumlah);
                pst.executeUpdate();
                update_stok(kode, jumlah);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Terjadi kesalahan pada simpan item belanja: Details\n" + ex.toString());
            }
        }
    }

    private void update_stok(String kode, int jumlah) {
        try {
            sql = "update tb_barang set stok=stok-?where kode_barang=?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, jumlah);
            pst.setString(2, kode);
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Terjadi kesalahan pada update stok:" + ex.toString());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        textNota = new javax.swing.JTextField();
        textKodeBarang = new javax.swing.JTextField();
        comboPelanggan = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        textNamaBarang = new javax.swing.JTextField();
        textKategori = new javax.swing.JTextField();
        buttonCariBarang = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        textIDPelanggan = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        textHarga = new javax.swing.JTextField();
        textStok = new javax.swing.JTextField();
        textQty = new javax.swing.JTextField();
        buttonTambah = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        textTotal = new javax.swing.JTextField();
        textBayar = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        textKembali = new javax.swing.JTextField();
        buttonBatal = new javax.swing.JButton();
        buttonSimpan = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelItemBelanja = new javax.swing.JTable();
        textTanggal = new com.toedter.calendar.JDateChooser();
        buttonHapus = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 204));

        jLabel1.setFont(new java.awt.Font("Tahoma", 3, 24)); // NOI18N
        jLabel1.setText("FORM TRANSAKSI");

        jLabel2.setText("NO NOTA");

        jLabel3.setText("NAMA PELANGGAN");

        comboPelanggan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboPelangganActionPerformed(evt);
            }
        });

        jLabel4.setText("KODE BARANG");

        jLabel5.setText("NAMA BARANG");

        jLabel6.setText("KATEGORI");

        buttonCariBarang.setText("CARI");
        buttonCariBarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCariBarangActionPerformed(evt);
            }
        });

        jLabel7.setText("TANGGAL TRANSAKSI");

        jLabel8.setText("HARGA");

        jLabel9.setText("STOK");

        jLabel10.setText("JUMLAH BELI");

        textQty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textQtyActionPerformed(evt);
            }
        });

        buttonTambah.setText("TAMBAH");
        buttonTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTambahActionPerformed(evt);
            }
        });

        jLabel11.setText("TOTAL");

        jLabel12.setText("BAYAR");

        textBayar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textBayarKeyReleased(evt);
            }
        });

        jLabel13.setText("KEMBALI");

        buttonBatal.setText("BATAL");
        buttonBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBatalActionPerformed(evt);
            }
        });

        buttonSimpan.setText("SIMPAN");
        buttonSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSimpanActionPerformed(evt);
            }
        });

        tabelItemBelanja.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Kode Barang", "Nama Barang", "Kategori", "Harga", "Satuan", "Total"
            }
        ));
        jScrollPane1.setViewportView(tabelItemBelanja);

        buttonHapus.setText("HAPUS");
        buttonHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonHapusActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6))
                        .addGap(41, 41, 41)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(textNamaBarang, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
                                    .addComponent(textKategori)
                                    .addComponent(textKodeBarang))
                                .addGap(18, 18, 18)
                                .addComponent(buttonCariBarang)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel10)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(textNota, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel7))
                            .addComponent(comboPelanggan, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(textIDPelanggan)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(textQty, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(buttonTambah))
                            .addComponent(textHarga)
                            .addComponent(textStok)
                            .addComponent(textTanggal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))
                        .addGap(60, 60, 60)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(textBayar, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                            .addComponent(textTotal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 130, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(buttonBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(buttonSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(textKembali, javax.swing.GroupLayout.PREFERRED_SIZE, 406, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(228, 228, 228))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(489, 489, 489)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(560, 560, 560)
                        .addComponent(buttonHapus)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(textNota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addGap(32, 32, 32)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(textKodeBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4)
                                    .addComponent(buttonCariBarang)
                                    .addComponent(jLabel8)
                                    .addComponent(textHarga, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel5)
                                            .addComponent(textNamaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(5, 5, 5)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(textKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(textStok, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(3, 3, 3))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(buttonTambah)
                                        .addComponent(textQty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(73, 73, 73)
                        .addComponent(textTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(comboPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textIDPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(buttonHapus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(textKembali, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(buttonBatal)
                    .addComponent(buttonSimpan))
                .addGap(31, 31, 31))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSimpanActionPerformed
        simpan_transaksi();
    }//GEN-LAST:event_buttonSimpanActionPerformed

    private void buttonTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTambahActionPerformed
        if (textKodeBarang.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Data barang belum dipilih");
        } else if (textQty.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Jumlah beli belum diisi");
        } else if (Integer.parseInt(textQty.getText()) > Integer.parseInt(textStok.getText())) {
            JOptionPane.showMessageDialog(null, "Stok barang tidak cukup!");
            textQty.setText("0");
            textQty.requestFocus();
        } else if (Integer.parseInt(textQty.getText()) <= 0) {
            JOptionPane.showMessageDialog(null, "Jumlah beli tidak boleh dibawah noll");
            textQty.setText("0");
            textQty.requestFocus();
        } else {
            dtm = (DefaultTableModel) tabelItemBelanja.getModel();
            ArrayList list = new ArrayList();
            list.add(textKodeBarang.getText());
            list.add(textNamaBarang.getText());
            list.add(textKategori.getText());
            list.add(textHarga.getText());
            list.add(textQty.getText());
            list.add(Integer.parseInt(textHarga.getText()) * Integer.parseInt(textQty.getText()));

            dtm.addRow(list.toArray());
            textKodeBarang.setText("");
            textNamaBarang.setText("");
            textKategori.setText("");
            textHarga.setText("");
            textStok.setText("");
            textQty.setText("");
            hitung_total();
        }
    }//GEN-LAST:event_buttonTambahActionPerformed

    private void textQtyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textQtyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textQtyActionPerformed

    private void comboPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboPelangganActionPerformed
        try {
            pst = conn.prepareStatement("select id_pelanggan from tb_pelanggan where nama_pelanggan=?");
            pst.setString(1, comboPelanggan.getSelectedItem().toString());
            rs = pst.executeQuery();
            if (rs.next()) {
                textIDPelanggan.setText(rs.getString(1));
               // JOptionPane.showMessageDialog(null, textIDPelanggan.getText());
            }
        } catch (SQLException ex) {
            Logger.getLogger(TransaksiView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_comboPelangganActionPerformed

    private void buttonBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBatalActionPerformed
        ulang();
    }//GEN-LAST:event_buttonBatalActionPerformed

    private void buttonCariBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCariBarangActionPerformed
        CariBarangView cbv = new CariBarangView(null, true);//import bila error
        cbv.setVisible(true);
    }//GEN-LAST:event_buttonCariBarangActionPerformed

    private void buttonHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonHapusActionPerformed
        int row = tabelItemBelanja.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Pilih dulu item yang mau dihapus");
        } else {
            dtm.removeRow(row);
            tabelItemBelanja.setModel(dtm);
            hitung_total();
        }
    }//GEN-LAST:event_buttonHapusActionPerformed

    private void textBayarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textBayarKeyReleased
        BigDecimal bayar = new BigDecimal(0);
        if (!textBayar.getText().equals("")) {
            bayar = new BigDecimal(textBayar.getText());
        }
        BigDecimal total = new BigDecimal(textBayar.getText());
        BigDecimal kembali = bayar.subtract(total);
        textKembali.setText(kembali.toString());
    }//GEN-LAST:event_textBayarKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonBatal;
    private javax.swing.JButton buttonCariBarang;
    private javax.swing.JButton buttonHapus;
    private javax.swing.JButton buttonSimpan;
    private javax.swing.JButton buttonTambah;
    private javax.swing.JComboBox<String> comboPelanggan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabelItemBelanja;
    private javax.swing.JTextField textBayar;
    public static javax.swing.JTextField textHarga;
    private javax.swing.JTextField textIDPelanggan;
    public static javax.swing.JTextField textKategori;
    private javax.swing.JTextField textKembali;
    public static javax.swing.JTextField textKodeBarang;
    public static javax.swing.JTextField textNamaBarang;
    private javax.swing.JTextField textNota;
    private javax.swing.JTextField textQty;
    public static javax.swing.JTextField textStok;
    private com.toedter.calendar.JDateChooser textTanggal;
    private javax.swing.JTextField textTotal;
    // End of variables declaration//GEN-END:variables
}
