package pelisrecuperacion;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author guerr
 */
public class ConectaBD {

    //LAS VARIABLES SON GLOBALES 
    protected Connection conexion;
    protected Statement sentencia;
    /**
     * Resulset es una tabla virtual donde se almacenan los datos que queremos
     * ver por lo tanto no sera nesario para insertar borrar o modificar.
     *
     */
    protected ResultSet resultSet;

    /**
     * Conectar directamente con el usuario catastro autocommit DESACTIVADO
     */
    public ConectaBD() {

        //Conexion con catastro catastro
        try {
            //Hay dos formas de cargar el Driver. Creando un nuevo objeto o utilizando la clase
            //Class.forName(“oracle.jdbc.driver.OracleDriver”); 
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());//Es el recomendado por Oracle.
            //conexion con url 
            conexion = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "catastro", "catastro");
            conexion.setAutoCommit(false);
        } catch (SQLException e) {
            System.out.println("ERROR AL CONECTAR");
        }
    }

    /**
     * Conexion con algun usuario en Oracle o MySql
     *
     * @param tipoBase Oracle o Mysql
     * @param nBaseDatos Nombre de la base de datos para MySql, null para
     * oracle.
     * @param usuario
     * @param contraseña autocommit DESACTIVADO
     */
    public ConectaBD(String tipoBase, String nBaseDatos, String usuario, String contraseña) {

        if (tipoBase.equalsIgnoreCase("Oracle")) {
            try {
                DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
                //conexion con url
                conexion = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", usuario, contraseña);
                conexion.setAutoCommit(false);
            } catch (SQLException ex) {
                System.out.println("ERROR AL CONECTAR");
            }
        } else if (tipoBase.equalsIgnoreCase("MySql")) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conexion = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + nBaseDatos, usuario, contraseña);
                conexion.setAutoCommit(false);
            } catch (ClassNotFoundException | SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public Connection getConexion() {
        return conexion;
    }

    public Statement getSentencia() {
        return sentencia;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    /**
     * Devuelve el numero de filas afectadas por un delete, update o insert No
     * hace commit
     *
     * @param instruccion Instruccion a afectar (Insert, Update o Delete)
     * @return Números de filas afectadas
     */
    public int ejecutarInstruccion(String instruccion) {

        int filas = 0;

        try {
            sentencia = conexion.createStatement();
            filas = sentencia.executeUpdate(instruccion);
        } catch (SQLException ex) {
            System.out.println("Error ejecutarInstruccion" + ex);
        }

        return filas;
    }

    /**
     * Finaliza la transaccion, aceptando los cambios en la base de datos
     */
    public void commit() {

        try {
            conexion.commit();
        } catch (SQLException ex) {
            System.out.println("Error commit" + ex);
        }
    }

    /**
     * Vuelve a un estado previo a la base de datos
     */
    public void rollback() {

        try {
            conexion.rollback();
        } catch (SQLException ex) {
            System.out.println("Error rollback");
        }
    }

    /**
     * Cierra el ResultSet
     */
    public void cerrarResult() {
        try {
            resultSet.close();
        } catch (SQLException ex) {
            System.out.println("Error cerrarResult" + ex);
        }
    }

    /**
     * Cierra la sentencia
     */
    public void cerrarSentencia() {
        try {
            sentencia.close();
        } catch (SQLException ex) {
            System.out.println("Error cerrarSentencia" + ex);
        }
    }

    /**
     * Cierra la conexion
     */
    public void cerrarConexion() {
        try {
            if (resultSet != null) {
                cerrarResult();
            }
            if (sentencia != null) {
                cerrarSentencia();
            }
            conexion.close();
        } catch (SQLException ex) {
            System.out.println("Error cerrarConexion");
        }
    }

    /**
     * Devuelve al resultset los resultados de una consulta
     *
     * @param consulta Consulta a ejecutar
     * @return ResultSet
     */
    public ResultSet ejecutarConsulta(String consulta) {
        try {
            sentencia = conexion.createStatement();
            resultSet = sentencia.executeQuery(consulta);

        } catch (SQLException ex) {
            System.out.println("Error ejecutarConsulta");
        }

        return resultSet;
    }

    /**
     * Ejecuta una intrucción y podemos indicar si queremos hacer o no commit
     *
     * @param instruccion Instruccion a ejecutar
     * @param commit True = ejecuta también el commit
     * @return Número de filas afectadas por un delete, update o insert
     */
    public int ejecutarInstruccionCommit(String instruccion, boolean commit) {

        int filas = 0;

        try {
            sentencia = conexion.createStatement();
            filas = sentencia.executeUpdate(instruccion);

            if (commit) {
                commit();
            }

        } catch (SQLException ex) {
            System.out.println("error ejecutarInstruccionCommit");
        }

        return filas;
    }

    /**
     * A partir de un fichero de texto separado por comas se introducen los
     * registros en la tabla.
     *
     * @param tabla
     * @param fichero
     *
     */
    public void insertarTxt(String tabla, String fichero) {
        String ruta = "src/datos/";
        FileReader fr = null;
        Scanner scl = null, sc = null;
        String linea = "", insert = "";
        String campo = "";
        ArrayList<String> types = new ArrayList();

        try {
            // Procesamos el fichero..............
            fr = new FileReader(ruta + fichero);
            sc = new Scanner(fr);
            while (sc.hasNextLine()) {
                linea = sc.nextLine();
                scl = new Scanner(linea);
                scl.useDelimiter(",");
                //sentencia.executeQuery("select * from "+tabla);
                ejecutarConsulta("select data_type from user_tab_columns "
                        + "where table_name='" + tabla + "'");

                while (resultSet.next()) {
                    types.add(resultSet.getString("data_type"));
                }

                insert = "insert into " + tabla + " values (";
                //conn.getMetaData().getTables(null, null, tabla, types);
                int i = 0;
                while (scl.hasNext()) {
                    campo = scl.next();
                    if (types.get(i).equalsIgnoreCase("VARCHAR2")) {
                        insert += "'" + campo + "',";
                    } else {
                        insert += campo + ",";
                    }
                    i++;
                }
                insert = insert.substring(0, insert.length() - 1);
                insert += ")";
                System.out.println("insert= " + insert);
                sentencia.executeUpdate(insert);
                sentencia.close();
            }

        } catch (FileNotFoundException ex) {
            System.out.println("Fichero no encontrado");
        } catch (SQLException ex) {
            System.out.println("Se ha producido un error al insertar " + ex);
        }
    }//

    /**
     * Cuenta el numero de registros
     *
     * @param tabla Nombre de la tabla de la base de datos
     * @param condicion Condicion (sin where)
     * @return Número de registros de la consulta
     */
    public int cuentaRegistrosConsulta(String tabla, String condicion) {

        String consulta;

        if (condicion.equals("")) {
            consulta = "select count(*) from " + tabla;
        } else {
            consulta = "select count(*) from " + tabla + " where " + condicion;
        }

        try (Statement sentenciaAux = conexion.createStatement();
                ResultSet aux = sentenciaAux.executeQuery(consulta);) {

            return aux.getInt(0);

        } catch (SQLException ex) {

            return -1;
        }

    }

    /**
     * Indica si el resutado de una consulta es vacia
     *
     * @param query Consulta, debe contener un count
     * @return True = consulta vacia
     */
    public boolean consultaVacia(String query) {

        boolean vacio = false;

        Statement sentenciaAux;
        try {
            sentenciaAux = conexion.createStatement();

            ResultSet aux = sentenciaAux.executeQuery(query);

            aux.next();

            if (aux.getInt(1) == 0) {
                vacio = true;
            }

            aux.close();
            sentenciaAux.close();

        } catch (SQLException ex) {
            System.out.println(ex);
        }

        return vacio;

    }

    /**
     * Devuelve un array de String con todos los valores String de la columna
     *
     * @param columna Nombre de la columna de la base de datos
     * @param tabla Nombre de la tabla de la base de datos
     *
     * @return Array con todos los valores String de la columna
     */
    public String[] devolverValoresString(String columna, String tabla) {

        String consulta = "select " + columna + " from " + tabla;
        try {
            Statement sentenciaAux = conexion.createStatement();

            consulta = "select count(*) from " + tabla;
            ResultSet aux = sentenciaAux.executeQuery(consulta);

            int total = cuentaRegistrosConsulta(tabla, "");

            String valores[] = new String[total];

            for (int i = 0; aux.next(); i++) {
                valores[i] = aux.getString(1);
            }

            return valores;

        } catch (SQLException ex) {
            System.out.println(ex);
            return null;
        }

    }

    /**
     * Indica si exite el valor que le indicamos Recomendable para valores
     * únicos de String
     *
     * @param valor Valor que queremos saber si existe
     * @param columna Nombre de la columna de la base de datos
     * @param tabla Nombre de la tabla de la columna de la base de datos
     * @return Indica si existe o no el valor
     */
    public boolean existeValor(String valor, String columna, String tabla) {

        boolean existe = false;

        Statement sentenciaAux;
        try {
            sentenciaAux = conexion.createStatement();

            ResultSet aux = sentenciaAux.executeQuery("select count(*) from " + tabla + " where upper(" + columna + ")='" + valor.toUpperCase() + "'");

            aux.next();

            if (aux.getInt(1) >= 1) {
                existe = true;
            }

            aux.close();
            sentenciaAux.close();

        } catch (SQLException ex) {
            System.out.println(ex);
        }

        return existe;
    }

    /**
     * Indica si exite el valor que le indicamos Recomendable para valores
     * unicos de int
     *
     * @param valor Valor que queremos saber si existe
     * @param columna Nombre de la columna de la base de datos
     * @param tabla Nombre de la tabla de la columna de la base de datos
     * @return Indica si existe o no el valor
     */
    public boolean existeValor(int valor, String tabla, String columna) {

        boolean existe = false;

        Statement sentenciaAux;

        try {
            sentenciaAux = conexion.createStatement();

            ResultSet aux = sentenciaAux.executeQuery("select count(*) from " + tabla + " where " + columna + "=" + valor + "");

            aux.next();

            if (aux.getInt(1) >= 1) {
                existe = true;
            }

            aux.close();
            sentenciaAux.close();

        } catch (SQLException ex) {
            System.out.println(ex);
        }

        return existe;
    }

    /**
     * @param tabla Nombre de la tabla
     *
     * @return numero de valores introducidos en la bbdd
     */
    public int insertarRegistroEnTablaUsuario(String tabla) {
        ArrayList<String> ColumnTypeName = new ArrayList<>();
        ArrayList<String> nombreDeColumna = new ArrayList<>();
        ArrayList<Integer> tam = new ArrayList<>();
        String campo, insert = "insert into " + tabla + " values (";

        int numValoresIntroducidos = 0;

        try {
            ResultSet typeInfo = conexion.getMetaData().getColumns(null, null, tabla, "%");

            while (typeInfo.next()) {
                ColumnTypeName.add(typeInfo.getString(6));
                nombreDeColumna.add(typeInfo.getString(4));
                tam.add(typeInfo.getInt(7));
            }

            for (int i = 0; i < ColumnTypeName.size(); i++) {

                Scanner s = new Scanner(System.in);
                System.out.println("EL siguiente dato a introducir es " + nombreDeColumna.get(i) + " De tipo " + ColumnTypeName.get(i) + " (max size " + tam.get(i) + ")");
                campo = s.next();

                if (ColumnTypeName.get(i).equalsIgnoreCase("DATE")) {
                    insert += "TO_DATE('" + campo + "', " + "'DD/MM/YYYY'), ";
                } else if (ColumnTypeName.get(i).equalsIgnoreCase("VARCHAR2")) {
                    insert += "'" + campo + "',";
                } else {
                    insert += campo + ",";
                }

            }

            sentencia = conexion.createStatement();
            insert = insert.substring(0, insert.length() - 1);
            insert += ")";
            numValoresIntroducidos = sentencia.executeUpdate(insert);

        } catch (SQLException ex) {
            System.out.println("Error sql" + ex);
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("fuera de indice");
        }

        return numValoresIntroducidos;

    }

    /**
     * Lee el resulset y lo muestra por pantalla con las columnas
     *
     * @throws SQLException
     */
    public void readResultSet() {

        //BufferedWriter bw = null;
        //ArrayList<Integer> arrai = new ArrayList<>();
        try {
            /* Obtengo el objeto MetaData del ResultSet */
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            //bw = new BufferedWriter(new FileWriter("C:\\Users\\guerr\\Desktop\\datos.txt", true));
            /* Recorro el número de columnas y muestro el nombre de las columnas */
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnLabel(i) + "\t\t\t");
                // int longitud = resultSetMetaData.getColumnLabel(i).length();
                //arrai.add(longitud);
                //  bw.write("   " + resultSetMetaData.getColumnLabel(i) + "\t");
            }

            /* Salto de línea para empezar a mostrar los valores de las columnas */
            System.out.println();
            // bw.newLine();

            /* 
             * Recorro las filas que contiene el ResultSet 
             * y muestro sus valores
             */
            while (resultSet.next()) {
                /* Recorro los valores de la fila y los muestro */
                for (int l = 1; l <= resultSetMetaData.getColumnCount(); l++) {
                    System.out.print("" + resultSet.getString(l) + "\t\t");

//                    if (resultSet.getString(l).length() > arrai.get(l - 1)) {
//
//                        bw.write("  " + resultSet.getString(l) + "\t"); //
//                    } else {
//                        bw.write("      " + resultSet.getString(l) + "\t");
//                    }
                }


                /* Salto de linea para mostrar la siguiente fila */
                System.out.println();
                // bw.newLine();
            }
            // bw.close();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    /**
     * @return Devuelve un archivo html con los resultados en forma de tabla con
     * sus cabeceras
     * @param html no es necesario pero lo uso para sobrecargar el metodo.
     */
    public void readResultSet(String html) {

        BufferedWriter bw = null;
        ArrayList<Integer> arrai = new ArrayList<>();

        try {

            /* Obtengo el objeto MetaData del ResultSet */
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            bw = new BufferedWriter(new FileWriter("C:\\Users\\guerr\\Desktop\\datos.html", true));
            bw.write("<table border=\"1\", align=\"center\">");
            bw.write("<tr>");
            /* Recorro el número de columnas y muestro el nombre de las columnas */
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnLabel(i) + "\t");
                int longitud = resultSetMetaData.getColumnLabel(i).length();
                arrai.add(longitud);

                bw.write("<th>" + resultSetMetaData.getColumnLabel(i) + "</th>");

            }
            bw.write("</tr>");

            /* Salto de línea para empezar a mostrar los valores de las columnas */
            System.out.println();
            bw.newLine();

            /* 
             * Recorro las filas que contiene el ResultSet 
             * y muestro sus valores
             */
            while (resultSet.next()) {
                bw.write("<tr>");
                /* Recorro los valores de la fila y los muestro */
                for (int l = 1; l <= resultSetMetaData.getColumnCount(); l++) {
                    System.out.print("      " + resultSet.getString(l) + "\t");

                    bw.write("<td align=\"center\">" + resultSet.getString(l) + "</td>"); //

                }
                bw.write("</tr>");


                /* Salto de linea para mostrar la siguiente fila */
                System.out.println();
                bw.write("<tr>");
            }
            bw.write("</tr>");
            bw.write("</table>");
            bw.close();
        } catch (SQLException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            Logger.getLogger(ConectaBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metodo que borra datos de una tabla de la base de datos; borrara las
     * filas de la tabla dada que cumplan una unica condicion
     *
     *
     *
     * @return numero de filas borradas en la operacion
     *
     */
    public int borraDatosBd() {

        int filasborradas = 0;

        System.out.println("NOMBRE DE LA TABLA QUE QUIERES BORRAR: ");
        Scanner s = new Scanner(System.in);
        String tabla = s.next();
        s.close();
        System.out.println("NOMBRE DE LA COLUMNA: ");
        Scanner sc = new Scanner(System.in);
        String nombreCol = sc.next();
        sc.close();
        System.out.println("CONDICION A CUMPLIR ");
        Scanner scc = new Scanner(System.in);
        String condicion = scc.next();
        String instruccion = "DELETE FROM " + tabla + "WHERE " + nombreCol + " = " + condicion;
        filasborradas = ejecutarInstruccionCommit(instruccion, true);
        return filasborradas;
    }

    /**
     * Metodo que actuliza una columna de la base de datos
     *
     * @param tabla nombre de la tabla
     * @param columna nombre de la columna a actualizar
     * @param valor nuevo valor
     * @param condicion condicion a cumplir
     * @return numero de filas actualizadas
     */
    public int actualizaBda(String tabla, String columna, String valor, String condicion) {
        String instruccion = "UPDATE FROM " + tabla + "SET" + columna + "=" + valor + "WHERE " + condicion;
        int filasborradas = ejecutarInstruccionCommit(instruccion, true);
        return filasborradas;
    }

    public int actualizaBd() {

        System.out.println("NOMBRE DE LA TABLA QUE QUIERES BORRAR: ");
        Scanner s = new Scanner(System.in);
        String tabla = s.next();
        s.close();
        System.out.println("NOMBRE DE LA COLUMNA: ");
        Scanner sc = new Scanner(System.in);
        String nombreCol = sc.next();
        sc.close();
        System.out.println("CONDICION A CUMPLIR: ");
        Scanner scc = new Scanner(System.in);
        String condicion = scc.next();
        System.out.println("NUEVO VALOR: ");
        Scanner sca = new Scanner(System.in);
        String valor = sca.next();
        int actualizaBda = actualizaBda(tabla, nombreCol, valor, condicion);
        return actualizaBda;
    }

    //
    public JScrollPane TablaFormada(String tabla) {
        DefaultTableModel defaultTableModel = new DefaultTableModel();

        ejecutarConsulta("Select * from " + tabla);
        try {
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columns = metadata.getColumnCount(); // Get number of columns

            // Array to hold names
// Get the column names
            for (int i = 0; i < columns; i++) {

                defaultTableModel.addColumn(metadata.getColumnLabel(i + 1));

            }
// Get all rows
            String[] rowData; // Stores one row
            while (resultSet.next()) { // For each row...
                rowData = new String[columns]; // create array to hold the data
                for (int i = 0; i < columns; i++) { // For each column
                    rowData[i] = resultSet.getString(i + 1); // retrieve the data item

                }

                defaultTableModel.addRow(rowData);
            }

        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
        JTable jTable = new JTable(defaultTableModel);
        TableRowSorter tableRowSorter = new TableRowSorter(defaultTableModel);
        // tableRowSorter.setRowFilter(RowFilter.regexFilter("AND"));
        jTable.setRowSorter(tableRowSorter);
        JScrollPane jScrollPane = new JScrollPane(jTable);

        return jScrollPane;
    }

    /**
     *
     * @param tabla
     * @return
     */
    public DefaultTableModel tableModel(String tabla) {
        DefaultTableModel defaultTableModel = new DefaultTableModel();

        ejecutarConsulta("Select * from " + tabla);
        try {
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columns = metadata.getColumnCount(); // Get number of columns

            // Array to hold names
// Get the column names
            for (int i = 0; i < columns; i++) {

                defaultTableModel.addColumn(metadata.getColumnLabel(i + 1));

            }
// Get all rows
            String[] rowData; // Stores one row
            while (resultSet.next()) { // For each row...
                rowData = new String[columns]; // create array to hold the data
                for (int i = 0; i < columns; i++) { // For each column
                    rowData[i] = resultSet.getString(i + 1); // retrieve the data item

                }

                defaultTableModel.addRow(rowData);
            }

        } catch (SQLException sqle) {
            System.err.println(sqle);
        }

        return defaultTableModel;
    }

    public void cargaCombo(JComboBox<Object> combo, String tabla) {

        try {
            ResultSet ejecutarConsulta = ejecutarConsulta("select * from " + tabla);

            ResultSetMetaData metaData = ejecutarConsulta.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (ejecutarConsulta.next()) {
                for (int i = 1; i < columnCount; i++) {

                    combo.addItem(ejecutarConsulta.getObject(i));
                    
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(ConectaBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
     
}
