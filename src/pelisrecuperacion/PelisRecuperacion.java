/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pelisrecuperacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Usuario
 */
public class PelisRecuperacion  extends JFrame{
    ImageIcon imagen;
    JLabel labelImagen;
    ConectaBD bD = new ConectaBD();
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PelisRecuperacion ventana = new PelisRecuperacion();
        ventana.setSize(900, 600);
        ventana.setLocationRelativeTo(null);
        ventana.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        ventana.setVisible(true);
    }

    public PelisRecuperacion() {
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                cerrar();
            }
        });
        //PANEL CENTRO----------------------------------------------------------
        JPanel centro = new JPanel();
        centro.setSize(new Dimension(500, 450));
        centro.setBorder(BorderFactory.createLineBorder(Color.yellow));
        JLabel labelColumnas = new JLabel("Columnas");
        JLabel labelDatoB = new JLabel("Dato a buscar: ");
        JComboBox comboColum = new JComboBox();
        JTextField fieldBuscar = new JTextField(15);
        
        
        DefaultTableModel modeloTabla = tableModel("peliculas");
        
        
        JTable tablaPelis = new JTable(modeloTabla){
            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 4:
                        return Boolean.class;
                    case 5:
                        return Boolean.class;
                    default:
                        return String.class;
                }
                //return null;
            }
        };
        tablaPelis.setSize(450, 300);
        JScrollPane scrollPelis = new JScrollPane(tablaPelis);
        //PANEL CENTRO SUR------------------------------------------------------
        JPanel centroSur = new JPanel();
        centroSur.setBorder(BorderFactory.createLineBorder(Color.red));
        JTable tablaActores = new JTable();
        JScrollPane scrollActores = new JScrollPane(tablaActores);
        JButton botonGuardar = new JButton("Guardar Datos");
        JTextField fieldGuardar = new JTextField(15);
        
        centro.add(labelColumnas);
        centro.add(comboColum);
        centro.add(labelDatoB);
        centro.add(fieldBuscar);
        centro.add(scrollPelis);
        centroSur.add(scrollActores);
        centroSur.add(botonGuardar);
        centroSur.add(fieldGuardar);
        centro.add(centroSur);
        
        
        
        //PANEL ESTE------------------------------------------------------------
        JPanel este = new JPanel(new BorderLayout());
        este.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        este.setSize(new Dimension(400, 450));
        JSlider sliderNorte = new JSlider(0, 400, 200);
        sliderNorte.setFont(new Font("Serif", Font.PLAIN, 12));
        sliderNorte.setMajorTickSpacing(50);
        sliderNorte.setMinorTickSpacing(25);
        sliderNorte.setPaintTicks(true);
        sliderNorte.setPaintLabels(true);
        JSlider sliderEste = new JSlider(JSlider.VERTICAL, 0, 400, 200);
        sliderEste.setFont(new Font("Serif", Font.PLAIN, 12));
        sliderEste.setMajorTickSpacing(50);
        sliderEste.setMinorTickSpacing(25);
        sliderEste.setPaintTicks(true);
        sliderEste.setPaintLabels(true);
        JLabel labelCaratula = new JLabel(); // <-aqui metes la foto
        JButton botonCerrar = new JButton("Cerrar Aplicacion");
        botonCerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cerrar();
            }
        });
        
        este.add(sliderNorte, BorderLayout.NORTH);
        este.add(labelCaratula, BorderLayout.CENTER);
        este.add(sliderEste, BorderLayout.EAST);
        este.add(botonCerrar, BorderLayout.SOUTH);
        String uri = null;
        
        
        
        
        //PANEL SUR-------------------------------------------------------------
        JPanel sur = new JPanel();
        JScrollPane scrollSur = new JScrollPane(sur);
        scrollSur.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //FILE PARA BUSCAR LAS CARATULAS----------------------------------------
        File fileCaratulas = new File("src/caratulas");
        String [] lista = fileCaratulas.list();
        
        for (int i = 0; i < lista.length; i++) {
            
            imagen = new ImageIcon("src/caratulas/"+lista[i]);
            Image image = imagen.getImage();
            Image newimage = image.getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH);
            imagen = new ImageIcon(newimage);
            
            labelImagen = new JLabel(imagen);
            sur.add(labelImagen);
        }
        
        
        
        add(centro, BorderLayout.CENTER);
        add(scrollSur, BorderLayout.SOUTH);
        add(este, BorderLayout.EAST);
    }
    
    static void cerrar(){
        String [] botones = {"Sí", "No"};
                int respuesta = JOptionPane.showOptionDialog(null, "Desea Salir?", "Ventana de salida", 
                        JOptionPane.WARNING_MESSAGE, 0, null, botones, botones[1]);
                if (respuesta == 0){
                    System.exit(0);
                }
    }
    
    public DefaultTableModel tableModel(String tabla) {
        DefaultTableModel defaultTableModel = new DefaultTableModel();

        ResultSet ejecutarConsulta = bD.ejecutarConsulta("Select * from " + tabla);
        try {
            ResultSetMetaData metadata = ejecutarConsulta.getMetaData();
            int columns = metadata.getColumnCount(); // Get number of columns

            // Array to hold names
// Get the column names
            for (int i = 0; i < columns; i++) {
                defaultTableModel.addColumn(metadata.getColumnLabel(i + 1));
            }
// Get all rows
            String[] rowData; // Stores one row
            while (ejecutarConsulta.next()) { // For each row...
                rowData = new String[columns]; // create array to hold the data
                for (int i = 0; i < columns; i++) { // For each column
                    rowData[i] = ejecutarConsulta.getString(i + 1); // retrieve the data item
                }

                defaultTableModel.addRow(rowData);
            }

        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
        return defaultTableModel;
    }
    
            
    
    
}
