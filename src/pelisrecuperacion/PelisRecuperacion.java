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
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        //PANEL NORTE-----------------------------------------------------------
        JPanel norte = new JPanel();
        JLabel labelColumnas = new JLabel("Columnas");
        JLabel labelDatoB = new JLabel("Dato a buscar: ");
        JComboBox comboColum = new JComboBox();
        cargaCombo(comboColum, "peliculas");
        JTextField fieldBuscar = new JTextField(15);
        norte.add(labelColumnas);
        norte.add(comboColum);
        norte.add(labelDatoB);
        norte.add(fieldBuscar);
        //PANEL CENTRO----------------------------------------------------------
        JPanel centro = new JPanel(new BorderLayout());
        centro.setSize(new Dimension(500, 450));
        centro.setBorder(BorderFactory.createLineBorder(Color.yellow));
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
        scrollPelis.setPreferredSize(new Dimension(600, 270));
        

        //PANEL CENTRO SUR------------------------------------------------------
        JPanel centroSur = new JPanel(new BorderLayout());
        centroSur.setPreferredSize(new Dimension(600, 130));
        centroSur.setBorder(BorderFactory.createLineBorder(Color.red));
        JTable tablaActores = new JTable();
        JScrollPane scrollActores = new JScrollPane(tablaActores);
        scrollActores.setPreferredSize(new Dimension(400, 90));
        cargarActores(tablaActores);
        JButton botonGuardar = new JButton("Guardar Datos");
        JTextField fieldGuardar = new JTextField(10);
        
        
        centro.add(scrollPelis, BorderLayout.CENTER);
        centroSur.add(scrollActores, BorderLayout.WEST);
        JPanel centroSurDer = new JPanel();
        centroSurDer.add(botonGuardar, BorderLayout.EAST);
        centroSurDer.add(fieldGuardar, BorderLayout.WEST);
        centroSur.add(centroSurDer);
        centro.add(centroSur, BorderLayout.SOUTH);
        
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
        add(norte, BorderLayout.NORTH);
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
    
    public void cargaCombo(JComboBox<Object> comboColum, String tabla) {

        try {
            ResultSet ejecutarConsulta = bD.ejecutarConsulta("select * from " + tabla);

            ResultSetMetaData metaData = ejecutarConsulta.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i < columnCount; i++) {

                comboColum.addItem(metaData.getColumnLabel(i));

            }

        } catch (SQLException ex) {
            Logger.getLogger(ConectaBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public DefaultTableModel tableModel(String tabla) {
        DefaultTableModel dtm = new DefaultTableModel();

        ResultSet ejecutarConsulta = bD.ejecutarConsulta("Select * from " + tabla);
        try {
            ResultSetMetaData metadata = ejecutarConsulta.getMetaData();
            int columns = metadata.getColumnCount(); // Get number of columns

            for (int i = 0; i < columns; i++) {
                dtm.addColumn(metadata.getColumnLabel(i + 1));
            }
            // Get all rows
            Object[] rowData; // Stores one row
            while (ejecutarConsulta.next()) { // For each row...
                rowData = new Object[columns]; // create array to hold the data
                for (int i = 0; i < columns; i++) { // For each column
                    String string = ejecutarConsulta.getString(i + 1);
                    rowData[i] = ejecutarConsulta.getString(i + 1); // retrieve the data item
                }
                for (int i = 4; i < 6; i++) {
                    if (((String) rowData[i]).equalsIgnoreCase("0")) {
                        rowData[i] = false;
                    } else {
                        rowData[i] = true;
                    }
                    //convertir celdas a checkbox
                    
                    
                }
                dtm.addRow(rowData);
            }
        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
        return dtm;
    }
    
    static void cargarActores(JTable tabla) {
        DefaultTableModel dtm = new DefaultTableModel();
        dtm.addColumn("Lista de actores");//hay que añadir la columna antes si no al final machaca con una columna vacia
        try {
            File file = new File("src/datos/actores.txt");
            Scanner s = new Scanner(file);
            
            while (s.hasNextLine()) {
                String linea = s.nextLine();
                String[] split = linea.split(",");
                String[] actores = split[0].split("-");
//                String actores = useDelimiter.nextLine();
//                String numero = useDelimiter.nextLine();
//                String[] actoresSeparados = actores.split("-");
        dtm.addRow(actores);
            }
             tabla.setModel(dtm);
             
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PelisRecuperacion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
