/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import exceptions.LimitException;
import com.fasterxml.jackson.annotation.JsonView;
import controller.AppCon;
import static data.Data.online_rows;
import static data.Data.online_version;
import dictionary.DictionaryString;
import exceptions.DateParseException;
import hierarchy.Hierarchy;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import jsoninterface.View;

/**
 *
 * @author nikos
 */
public class RelSetData implements Data {
    @JsonView(View.GetColumnNames.class)
    private String inputFile = null;
    private double relationalData[][] = null;
    private double setData[][] = null;
    private int sizeOfRows = 0;
    private int sizeOfCol = 0;
    private String delimeter = null;
    private String delimeterSet = null;
    @JsonView(View.SmallDataSet.class)
    private String[][] typeArr;
    
    @JsonView(View.DataSet.class)
    private int recordsTotal;
    @JsonView(View.SmallDataSet.class)
    private String[][] smallDataSet;
    @JsonView(View.GetDataTypes.class)
    private Map <Integer,String> colNamesType = null;
    private CheckVariables chVar = null;
    private Map <Integer,String> colNamesPosition = null;
    private DictionaryString dictionary = null;
    private DictionaryString dictHier = null;
    @JsonView(View.GetColumnNames.class)
    private String []columnNames = null;
    @JsonView(View.SmallDataSet.class)
    private String errorMessage = null;
    @JsonView(View.DataSet.class)
    private ArrayList<LinkedHashMap> data;
    @JsonView(View.DataSet.class)
    private int recordsFiltered;
    private int columnSetData;
    private int selectedColumn;
    private String[] formatsDate = null;
    
    
    private static final String[] formats = { 
                "yyyy-MM-dd'T'HH:mm:ss'Z'",   "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ss",      "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ss", 
                "MM/dd/yyyy HH:mm:ss",        "MM/dd/yyyy'T'HH:mm:ss.SSS'Z'", 
                "MM/dd/yyyy'T'HH:mm:ss.SSSZ", "MM/dd/yyyy'T'HH:mm:ss.SSS", 
                "MM/dd/yyyy'T'HH:mm:ssZ",     "MM/dd/yyyy'T'HH:mm:ss", 
                "yyyy:MM:dd HH:mm:ss",        "yyyy/MM/dd", 
                "yyyy:MM:dd HH:mm:ss.SS",      "dd/MM/yyyy",
                "dd MMM yyyy",                "dd-MMM-yyy"};  
    
    
    public RelSetData(String inputFile, String del,String delSet,DictionaryString dict){
        this.inputFile = inputFile;
        recordsTotal = 0;
        this.smallDataSet = null;
        colNamesType = new TreeMap<Integer,String>();
        colNamesPosition = new HashMap<Integer,String>();
        chVar = new CheckVariables();
        dictionary = new DictionaryString();
        this.dictHier = dict;
        selectedColumn=-1;
        
        
        this.inputFile = inputFile;
        if ( del == null ){
            delimeter = ",";
        }
        else{
            delimeter = del;
        }
        
        delimeterSet = delSet;
         
    }
    
    @Override
    public int getDataColumns() {
        return this.sizeOfCol;
    }

    @Override
    public double[][] getDataSet() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        if(selectedColumn == -1){
//            throw new UnsupportedOperationException("No selected column.");
//        }
//        
//        if(this.colNamesType.get(selectedColumn).equals("set")){
//            return setData;
//        }
//        else{
//            return relationalData;
//        }
        return relationalData;
    }

    @Override
    public void setData(double[][] _data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDataLenght() {
        return this.sizeOfRows;
    }

    @Override
    public void print() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exportOriginalData() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        try (PrintWriter writer = new PrintWriter(this.inputFile, "UTF-8")) {
            boolean FLAG = false;
                
            for(int i = 0 ; i < columnNames.length ; i ++){
                if (FLAG == false){
                    writer.print(columnNames[i]);
                    FLAG = true;
                }
                else{
                    writer.print(","+columnNames[i]);
                }

            }
            writer.println();
            
            for(int i=0; i<this.sizeOfRows; i++){
                for(int j=0; j<this.sizeOfCol; j++){
                    if (colNamesType.get(j).equals("double")){
                        if (Double.isNaN(relationalData[i][j])){
                            writer.print("");
                        }
                        else{
                            Object a = relationalData[i][j];
                            writer.print( relationalData[i][j]);
                        }
                    }
                    else if (colNamesType.get(j).equals("int")){
                        if (relationalData[i][j] == 2147483646.0){
                            writer.print("");
                        }
                        else{
                            writer.print( Integer.toString((int)relationalData[i][j])+"");
                        }
                    }
                    else if(colNamesType.get(j).equals("set")){
                        for(int l=0; l<setData[i].length; l++){
                            String str = dictionary.getIdToString((int) setData[i][l]);
                            if(str == null){
                                str = dictHier.getIdToString((int) setData[i][l]);
                            }
                            writer.print(str);
                            if(l!=setData[i].length-1){    
                                writer.print(this.delimeterSet);
                            }
                            
                        }
                    }
                    else{
                        String str = dictionary.getIdToString((int)relationalData[i][j]);
                        if( str == null){
                             str = dictHier.getIdToString((int) setData[i][j]);
                        }
    //                    DictionaryString dict = dictionary.get(j);
    //                    String str = dict.getIdToString((int)dataSet[i][j]);

                        if (str.equals("NaN")){
                            writer.print("");
                        }
                        else{
                            writer.print(str);
                        }
                    }
                    
                    if(j!=this.sizeOfCol-1){
                        writer.print(",");
                    }
                }
                writer.println();
            }
            
        }catch(FileNotFoundException | UnsupportedEncodingException ex) {
            //Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("done orgiginal data");
    }
    
    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }
    
    public String timestampToDate( String tmstmp){
      String [] temp = null;
        if ( tmstmp != null ) {
           for (String parse : formats) {
                SimpleDateFormat sdf = new SimpleDateFormat(parse);
                //System.out.println("edwwwwwww = ");
                try {
                    Date d1 = sdf.parse(tmstmp);
                    if (sdf.parse(tmstmp) != null){
                        if ( parse.equals("yyyy/MM/dd")){
                            if ( tmstmp.contains("/")){// den mporei na dei to dd/MM/yyyy giati prwta vlepei to yyyy//MM/dd
                                temp = tmstmp.split("/");
                                if (temp[0].length() <= 2){
                                    return tmstmp;
                                }
                                else{
//                                    System.out.println("parse = " + parse);
                                    //System.out.println("date111111" );
                                    SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");

                                    //Date date = sf.parse(tmstmp); 
                                    //System.out.println("date = " + date );
//                                    System.out.println("return = " +sf.format(d1));
                                    //System.out.println(date.);
                                    tmstmp = null;
                                    tmstmp = sf.format(d1);
                                    return tmstmp;
                                }
                            } 
                        }
                        else{
//                            System.out.println("parse = " + parse);
                            //System.out.println("date111111" );
                            SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");

                            //Date date = sf.parse(tmstmp); 
                            //System.out.println("date = " + date );
//                            System.out.println("return = " +sf.format(d1));
                            //System.out.println(date.);
                            tmstmp = null;
                            tmstmp = sf.format(d1);
                            return tmstmp;
                        }                        
                    }
 
                    //System.out.println("Printing the value of " + parse);
                } catch (ParseException e) {

                }
            }
        }
        
        return null;
    }

    @Override
    public String save(boolean[] checkColumns) throws DateParseException {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String []temp = null;
        String tempSet[] = null;
        String []colNames = null;
        ArrayList<String> columns = new ArrayList<String>();
        SimpleDateFormat sdf[] = new SimpleDateFormat[this.columnNames.length];
        SimpleDateFormat sdfDefault = new SimpleDateFormat("dd/MM/yyyy");
        int counter = 0;
        int counterSdf = 0;
        int stringCount;
        if(dictionary.isEmpty() && dictHier.isEmpty()){
            stringCount = 1;
        }
        else {
            stringCount = dictHier.getMaxUsedId()+1;
        }
        boolean FLAG = true;
        int counter1 = 0 ;
        
        try {
            fstream = new FileInputStream(inputFile);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
            
            while ((strLine = br.readLine()) != null){
                
                //do not read the fist line
                if (FLAG == true){
                    temp = strLine.split(delimeter,-1);
                    for ( int i = 0 ; i < temp.length ; i ++){
                        if (checkColumns[i] == true){
                            temp[i] = temp[i].trim().replaceAll("\"", "").replaceAll("[\uFEFF-\uFFFF]", "").replace(".", "").replace("[","(").replace("]", ")");;
                            if (temp[i].length() > 128){
                                temp[i] = temp[i].substring(0, 128);
                            }
                            columns.add(temp[i]);
                            if(colNamesType.get(counterSdf).contains("date")){
                                sdf[counterSdf] = new SimpleDateFormat(this.formatsDate[counterSdf]);
                            }
                            counterSdf++;
                        }
                    }
                    colNames = new String[columns.size()];
                    colNames = columns.toArray(new String[0]);
                    this.setColumnNames(colNames);
                    FLAG = false;
                    relationalData = new double[sizeOfRows][columnNames.length];
                    setData = new double[sizeOfRows][];
                }
                else if(strLine.trim().isEmpty()){
                    continue;
                }
                else{
                    
                    temp = strLine.split(delimeter,-1);
                    counter1 = 0;
                    for (int i = 0; i < temp.length ; i ++ ){
                        if (checkColumns[i] == true){
                            temp[i] = temp[i].trim().replaceAll("[\uFEFF-\uFFFF]", "");
                            if ( temp[i].equals("")){
                            }

                            if ( colNamesType.get(counter1).contains("int") ){
                                if ( !temp[i].equals("")){
                                    try {
                                        relationalData[counter][counter1] = Integer.parseInt(temp[i]);
                                    } catch (java.lang.NumberFormatException exc) {
                                        //ErrorWindow.showErrorWindow("Column : " + colNames[i] + " is chosen as integer and you have double values");
                                        System.out.println("Column : " + colNames[i] + " is chosen as integer and you have double values");
                                        return null;
                                    }   
                                }
                                else{
                                    relationalData[counter][counter1] = 2147483646;
                                }
                            }
                            else if (colNamesType.get(counter1).contains("double")){
                                if ( !temp[i].equals("")){
                                    temp[i] = temp[i].replaceAll(",", ".");
                                    relationalData[counter][counter1] = Double.parseDouble(temp[i]);
                                }
                                else{
                                    relationalData[counter][counter1] = Double.NaN;
                                }

                            }
                            else if (colNamesType.get(counter1).contains("date")){
//                                DictionaryString tempDict = dictionary.get(counter1);
                                String var = null;
//                                System.out.println("date= "+temp[i]+" counter= "+counter+" counter1= "+counter1+" ");
                                if ( !temp[i].equals("")){
                                    var = temp[i];
//                                    var = this.timestampToDate(var);
                                    try{
                                    if(this.formatsDate[counter1].equals("dd/MM/yyyy")){
                                        var = sdf[counter1].parse(var) == null ? null : var;
                                    }
                                    else{
                                        Date d = sdf[counter1].parse(var);
                                        var = sdfDefault.format(d);
                                    }
                                    }catch(ParseException pe){
                                        throw new DateParseException(pe.getMessage()+"\nDate format must be the same in column "+this.columnNames[counter1]+"\nFormat recognised is: "+formatsDate[counter1]);
                                    }
                                    
                                    if(var == null){
                                        var = "NaN";
                                    }
                                }
                                else {
                                    var = "NaN";
                                }

                                
                                //transform timestamp to date
                                //var = this.timestampToDate(var);
                                
                                if (var != null) {
                                    
                                //if string is not present in the dictionary
                                    if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
                                        if(var.equals("NaN")){
                                            dictionary.putIdToString(2147483646, var);
                                            dictionary.putStringToId(var,2147483646);
//                                        dictionary.put(counter1, tempDict);
                                            relationalData[counter][counter1] = 2147483646.0;
                                        }
                                        else{
                                            dictionary.putIdToString(stringCount, var);
                                            dictionary.putStringToId(var,stringCount);
    //                                        dictionary.put(counter1, tempDict);
                                            relationalData[counter][counter1] = stringCount;
                                            stringCount++;
                                        }
                                    }
                                    else{
                                        //if string is present in the dictionary, get its id
                                        if(dictionary.containsString(var)){
                                            int stringId = dictionary.getStringToId(var);
                                            relationalData[counter][counter1] = stringId;
                                        }
                                        else{
                                            int stringId = this.dictHier.getStringToId(var);
                                            relationalData[counter][counter1] = stringId;
                                        }
                                    }
                                }
                            }
                            else if(colNamesType.get(counter1).contains("set")){
                                tempSet = temp[i].split("\\"+this.delimeterSet,-1);
                                setData[counter] = new double[tempSet.length];
                                for(int j=0; j<tempSet.length; j++){
                                    String var = null;

                                    if ( !tempSet[j].equals("")){
                                        var = tempSet[j];
                                    }
                                    else {
                                        var = "NaN";
                                    }

                                    //if string is not present in the dictionary
                                    if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
                                        if(var.equals("NaN")){
                                            dictionary.putIdToString(2147483646, var);
                                            dictionary.putStringToId(var,2147483646);
//                                        dictionary.put(counter1, tempDict);
                                            setData[counter][j] = 2147483646.0;
                                        }
                                        else{
                                            dictionary.putIdToString(stringCount, var);
                                            dictionary.putStringToId(var,stringCount);
        //                                    dictionary.put(counter1, tempDict);
                                            setData[counter][j] = stringCount;
                                            stringCount++;
                                        }
                                    }
                                    else{
                                        //if string is present in the dictionary, get its id
                                        if(dictionary.containsString(var)){
                                            int stringId = dictionary.getStringToId(var);
                                            setData[counter][j] = stringId;
                                        }
                                        else{
                                            int stringId = dictHier.getStringToId(var);
                                            setData[counter][j] = stringId;
                                        }
                                        
                                    }
                                }
                                
                                relationalData[counter][counter1] = -1;
                                
                            }
                            else{
//                                DictionaryString tempDict = dictionary.get(counter1);
                                String var = null;

                                if ( !temp[i].equals("")){
                                    var = temp[i];
                                }
                                else {
                                    var = "NaN";
                                }

                                //if string is not present in the dictionary
                                if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
                                     if(var.equals("NaN")){
                                        dictionary.putIdToString(2147483646, var);
                                        dictionary.putStringToId(var,2147483646);
//                                        dictionary.put(counter1, tempDict);
                                        relationalData[counter][counter1] = 2147483646.0;
                                    }
                                    else{
                                        dictionary.putIdToString(stringCount, var);
                                        dictionary.putStringToId(var,stringCount);
    //                                    dictionary.put(counter1, tempDict);
                                        relationalData[counter][counter1] = stringCount;
                                        stringCount++;
                                    }
                                }
                                else{
                                    //if string is present in the dictionary, get its id
                                    if(dictionary.containsString(var)){
                                        int stringId = dictionary.getStringToId(var);
                                        relationalData[counter][counter1] = stringId;
                                    }
                                    else{
                                        int stringId = this.dictHier.getStringToId(var);
                                        relationalData[counter][counter1] = stringId;
                                    }
                                }
                            }
                            counter1++;
                        }
                    }
                    counter++;
                }
            }
            
            in.close();
            
            System.out.println("size row = " + relationalData.length + "\tsize column = " + relationalData[0].length);
            
            /*for ( int  i = 0; i < dataSet.length ; i ++){
                for ( int j=0; j < dataSet[i].length ; j ++ ){
                    System.out.print("data = " + dataSet[i][j]);
                }
                System.out.println();
            }*/
            
        }catch(DateParseException dpe){
            throw new DateParseException(dpe);
        }
        catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: "+e.getMessage());
        }
        return "OK";
    }
    
    public double[][] getSet(){
        return setData;
    }
    
    public String getSetDelimeter(){
        return this.delimeterSet;
    }
    
    public int getSetColumn(){
        return this.columnSetData;
    }

    @Override
    public void preprocessing() throws LimitException  {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String []temp = null;
        int i = 0;
        int counter = -1;
        
        try {
            fstream = new FileInputStream(inputFile);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
            
            //counts lines of the dataset
            while ((strLine = br.readLine()) != null)   {
                if(!strLine.trim().isEmpty()){
                    counter++;
                }
                if(AppCon.os.equals(online_version) && counter > online_rows){
                    throw new LimitException("Dataset is too large, the limit is "+online_rows+" rows, please download desktop version, the online version is only for simple execution.");
                }
            }
            
            //System.out.println("counter = " + counter);
            
            sizeOfRows = counter;
            recordsTotal = sizeOfRows;
            recordsFiltered = sizeOfRows;
            in.close();
            
        }catch (IOException e){
            System.err.println("Error: " + e.getMessage());
        }
    }

    @Override
    public String readDataset(String[] columnTypes, boolean[] checkColumns) throws LimitException, DateParseException {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        SaveClmnsAndTypeOfVar(columnTypes,checkColumns);
        preprocessing();
        String result = save(checkColumns);
        return result;
    }

    @Override
    public void export(String file, Object[][] initialTable, Object[][] anonymizedTable, int[] qids, Map<Integer, Hierarchy> hierarchies, Map<Integer, Set<String>> suppressedValues) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Object[][] temp = null;
        if ( initialTable != null ){
            //System.out.println("initial table");
            temp = initialTable;
        }
        else{
            //System.out.println("anonymized table");
            temp = anonymizedTable;
        }
        
        try {
            PrintWriter writer = new PrintWriter( file, "UTF-8");
            
            boolean FLAG = false;
                
            for(int i = 0 ; i < temp[0].length ; i ++){
                if (FLAG == false){
                    writer.print(columnNames[i]);
                    FLAG = true;
                }
                else{
                    writer.print(","+columnNames[i]);
                }

            }
            writer.println();
            
            
            for (int row = 0; row < temp.length; row++){
                for(int column = 0; column < temp[0].length; column++){
                    Object value = temp[row][column];


                    if (!value.equals("(null)")){
                        writer.print(value);
                    }
                   



                    if(column != temp[row].length-1){
                        writer.print(",");
                    }
                }
                writer.println();
            }
            writer.flush();
            writer.close();
        }catch (FileNotFoundException | UnsupportedEncodingException ex) {
            //Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Map<Integer, String> getColNamesPosition() {
        return colNamesPosition;
    }

    @Override
    public DictionaryString getDictionary() {
        return this.dictionary;
    }

    @Override
    public int getColumnByName(String column) {
        for(Integer i : this.colNamesPosition.keySet()){
            if(this.colNamesPosition.get(i).equals(column)){
                selectedColumn = i;
                return i;
            }
        }
        selectedColumn = -1;
        return -1;
    }

    @Override
    public String getColumnByPosition(Integer columnIndex) {
        return this.colNamesPosition.get(columnIndex);
    }

    @Override
    public void SaveClmnsAndTypeOfVar(String[] columnTypes, boolean[] checkColumns) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String []temp = null;
        String []colNames = null;
        boolean firstLineNames = true;
        String [] newFormatDate = null;
        boolean removedColumn = false;
        
        try{
            fstream = new FileInputStream(inputFile);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
            int counter = 0 ;
            
            while ((strLine = br.readLine()) != null)   {
                if (firstLineNames == true){
                    colNames = strLine.split(delimeter,-1);
                    for ( int i = 0 ; i < colNames.length ; i ++){
                        if ( checkColumns[i] == true){
                            colNamesType.put(counter,null);
                            colNames[i] = colNames[i].trim().replaceAll("\"", "").replaceAll("[\uFEFF-\uFFFF]", "").replace(".", "").replace("[","(").replace("]", ")");
                            if (colNames[i].length() > 128){
                                colNames[i] = colNames[i].substring(0, 128);
                            }
                            colNamesPosition.put(counter,colNames[i]);
                            counter++;
                        }
                    }
                    
                    if(counter != columnNames.length){
                        newFormatDate = new String[counter];
                        removedColumn = true;
                    }
                    
                    firstLineNames = false;
                    
                    
                }
            }
                
            counter = 0 ;
            for ( int i = 0 ; i < columnTypes.length ; i ++ ){
            //System.out.println("columnnnnnnnnnnnnnn = " + colNamesType.size());
                if ( checkColumns[i] == true){
                    if (columnTypes[i].equals("int")){
                        colNamesType.put(counter, "int");
                    }
                    else if (columnTypes[i].equals("double")){
                        colNamesType.put(counter, "double");
                    }
                    else if (columnTypes[i].equals("date")){
                        colNamesType.put(counter, "date");
    //                        dictionary.put(counter, new DictionaryString());
                        if(removedColumn){
                            newFormatDate[counter] = this.formatsDate[i]; 
                        }
                    }
                    else if(columnTypes[i].equals("set")){
                        colNamesType.put(counter, "set");
                        this.columnSetData = counter;
                    }
                    else{
                        colNamesType.put(counter, "string");
    //                        dictionary.put(counter, new DictionaryString());
                    }

                    counter++;
                }
            }
            
            if(counter!=columnTypes.length){
                this.columnNames =  colNamesPosition.values().toArray(new String[this.colNamesPosition.size()]);
                this.formatsDate = newFormatDate;
            }
            sizeOfCol = columnNames.length;
            in.close();
        }catch(Exception e){
            System.err.println("Error: "+e.getMessage());
        }
    }
    
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public String findColumnTypes() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String []temp = null;
        int counter = 0;
        boolean firstLine = true;
        boolean firstLineData = true;
        int setCol=-1;
        
        try{
            fstream = new FileInputStream(inputFile);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
            while ((strLine = br.readLine()) != null)   {
                if(firstLine){
                    temp = strLine.split(delimeter,-1);
                    columnNames = new String[temp.length];
                    smallDataSet = new String[6][temp.length];
                    this.formatsDate = new String[temp.length];
                    for ( int i = 0 ; i < temp.length ; i ++){
                        temp[i] = temp[i].trim().replaceAll("\"", "").replace(".", "").replaceAll("[\uFEFF-\uFFFF]", "").replace("[","(").replace("]", ")");;
                        if (temp[i].length() > 128){
                            columnNames[i] = temp[i].substring(0, 128);
                        }
                        else{
                            columnNames[i] = temp[i];
                        }
                        
                    }
                    firstLine = false;
                }
                else{
                    temp = strLine.split(delimeter,-1);
                    
                    if( temp.length != columnNames.length){
                        System.out.println("columnNames = " + columnNames.length +"\t temp = " + temp.length );
                        //ErrorWindow.showErrorWindow("Parse problem.Different size between title row and data row");
                        this.errorMessage = "1";//"Parse problem.Different size between title row and data row"
                        System.out.println("Parse problem.Different size between title row and data row");
                        return errorMessage;
                    }
                    
                    
                    if(firstLineData){
                        for ( int i = 0 ; i < temp.length ; i ++ ){
                            counter = 0;
                            temp[i] = temp[i].trim().replaceAll("[\uFEFF-\uFFFF]", "");;
                            if ( !temp[i].equals("")){
                                if (chVar.isInt(temp[i])){
                                    smallDataSet[counter][i] = "int";
                                }
                                else if (chVar.isDouble(temp[i])){
                                    smallDataSet[counter][i] = "double";
                                }
                                else if(chVar.isDate(temp[i])){
                                    smallDataSet[counter][i] = "date";
                                    this.formatsDate[i] = chVar.lastFormat;
                                }
                                else if(temp[i].contains(this.delimeterSet)){
                                 
                                    smallDataSet[counter][i] = "set";
                                    
                                }
                                else{  
                                    smallDataSet[counter][i] = "string";
                                }
                            }
                            
                            smallDataSet[++counter][i] = temp[i];
                        }
                        firstLineData = false;
                    }
                    else if(counter < 6){
                        for ( int i = 0 ; i < temp.length ; i ++ ){
                            temp[i] = temp[i].trim().replaceAll("[\uFEFF-\uFFFF]", "");
                            smallDataSet[counter][i] = temp[i];

                            if ( !temp[i].equals("")){
                                if ( smallDataSet[0][i] != null ){
                                    if (smallDataSet[0][i].equals("int")){
                                        if (!chVar.isInt(temp[i])){
                                            if (chVar.isDouble(temp[i])){
                                                smallDataSet[0][i] = "double";
                                            }
                                            else if(temp[i].contains(this.delimeterSet)){
                                                smallDataSet[0][i] = "set";
                                            }
                                            else {
                                                smallDataSet[0][i] = "string";
                                            }
                                        }
                                    }
                                    else if(smallDataSet[0][i].equals("double")){
                                        if(temp[i].contains(this.delimeterSet)){
                                            smallDataSet[0][i] = "set";
                                        }
                                        else if (!chVar.isInt(temp[i]) && !chVar.isDouble(temp[i])){
                                            smallDataSet[0][i] = "string";
                                        }
                                    }
                                }
                                else{
                                    System.out.print("edwwww oxi1 "+i);
                                    if (chVar.isInt(temp[i])){
                                        smallDataSet[0][i] = "int";
                                    }
                                    else if (chVar.isDouble(temp[i])){
                                        smallDataSet[0][i] = "double";
                                    }
                                    else if(chVar.isDate(temp[i])){
                                        smallDataSet[0][i] = "date";
                                        this.formatsDate[i] = chVar.lastFormat;
                                    }
                                    else if(temp[i].contains(this.delimeterSet)){
                                        smallDataSet[0][i] = "set";
                                    }
                                    else{  
                                        smallDataSet[0][i] = "string";
                                    }
                                }
                            }
                        }
                        counter ++;
                    }
                    else{
                        for (int i = 0 ; i < smallDataSet[0].length ; i ++ ){
                            if ( smallDataSet[0][i] == null){
                                System.out.print("edwwww oxi "+i);
                                smallDataSet[0][i]= "string";
                            }
                        }
                        
                        break;
                    }
                }
            }
            in.close();
        }catch(Exception e){
            System.err.println("Error: "+e.getMessage());
        }
        
//        for(int i=0; i<smallDataSet.length; i++){
//            for(int j=0; j<smallDataSet[i].length; j++){
//                if(smallDataSet[i][j].equals("set")){
//                    setCol=j;
//                    break;
//                }
//            }
//            if(setCol!=-1){
//                break;
//            }
//        }
//        
//        if(setCol!=-1){
//            for(int i=0; i<smallDataSet.length; i++){
//                smallDataSet[i][setCol] = "set" ;
//            }
//        }
        
        
//        for(int i=0; i<smallDataSet.length; i++){
//            System.out.println("Line "+i);
//            for(int j=0; j<smallDataSet[0].length; j++){
//                System.out.println("Small data set: "+smallDataSet[0][j]);
//            }
//        }
//        
//        for(int i=0; i<columnNames.length; i++){
//            System.out.println("ColummnNames: "+columnNames[i]);
//        }
        
        return "Ok";
    }

    @Override
    public String[][] getSmallDataSet() {
        return smallDataSet;
    }

    @Override
    public ArrayList<LinkedHashMap> getPage(int start,int length) {
        boolean firstValSet = true;
        data = new ArrayList<LinkedHashMap>();
        int counter = 0 ;
        
        LinkedHashMap linkedHashTemp = null;
        int max;
        
        if ( start + length <= sizeOfRows ){
            max = start + length;
        }
        else{
            max = sizeOfRows;
        }
        
        
        /*for ( int i = 0 ; i < dataSet.length ; i ++){
            for ( int j = 0 ; j < dataSet[i].length ; j ++){
                System.out.print(dataSet[i][j] +"," );
            }
            System.out.println();
        }
        System.out.println("Dictionary");
        Dictionary d;
        for (Map.Entry<Integer,Dictionary> entry : dictionary.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
            d = entry.getValue();
            System.out.println("\\\\\\");
            for (Map.Entry<Integer,String> entry1 : d.getIdToString().entrySet()) {
                System.out.println(entry1.getKey()+" : "+entry1.getValue());
                
            }
            System.out.println("End");
        }*/
                

        for ( int i = start ; i < max ; i ++){
            linkedHashTemp = new LinkedHashMap<>();
            for (int j = 0 ; j < colNamesType.size() ; j ++){
                //System.out.println("data = " + dataSet[i][j]);
                if (colNamesType.get(j).equals("double")){
                    if (Double.isNaN(relationalData[i][j])){
                        linkedHashTemp.put(columnNames[j],"");
                    }
                    else{
                        Object a = relationalData[i][j];
                        linkedHashTemp.put(columnNames[j], relationalData[i][j]);
                    }
                }
                else if (colNamesType.get(j).equals("int")){
                    if (relationalData[i][j] == 2147483646.0){
                        linkedHashTemp.put(columnNames[j],"");
                    }
                    else{
                        linkedHashTemp.put(columnNames[j], Integer.toString((int)relationalData[i][j])+"");
                    }
                }
                else if(colNamesType.get(j).equals("set")){
                    firstValSet = true;
                    for (int l = 0 ; l < setData[i].length ; l ++){
//                DictionaryString dict = dictionary.get(0);
                //System.out.println()
                        if (firstValSet){
                            String value = dictionary.getIdToString((int)setData[i][l]);
                            if(value == null){
                                value = dictHier.getIdToString((int)setData[i][l]);
                            }
                            linkedHashTemp.put(columnNames[j], value);
                        //System.out.println( dict.getIdToString((int)dataSet[i][j]));
                        //linkedHashTemp.put(columnNames[0], null);
                            firstValSet = false;
                        }
                        else{
                            String value = dictionary.getIdToString((int)setData[i][l]);
                            if(value == null){
                                value = dictHier.getIdToString((int)setData[i][l]);
                            }
                            linkedHashTemp.put(columnNames[j], linkedHashTemp.get(columnNames[j]) +this.delimeterSet+value);
                        }

                    }
                    
//                    String str = dictionary.getIdToString((int)setData[i][j]);
////                    DictionaryString dict = dictionary.get(j);
////                    String str = dict.getIdToString((int)dataSet[i][j]);
//
//                    if (str.equals("NaN")){
//                        linkedHashTemp.put(columnNames[j],"");
//                    }
//                    else{
//                        linkedHashTemp.put(columnNames[j], str);
//                    }
                }
                else{
                    String str = dictionary.getIdToString((int)relationalData[i][j]);
                    if(str == null){
                        str = dictHier.getIdToString((int)relationalData[i][j]);
                    }
//                    DictionaryString dict = dictionary.get(j);
//                    String str = dict.getIdToString((int)dataSet[i][j]);

                    if (str.equals("NaN")){
                        linkedHashTemp.put(columnNames[j],"");
                    }
                    else{
                        linkedHashTemp.put(columnNames[j], str);
                    }
                }
            }
            data.add(linkedHashTemp);
            counter ++;
            
        }
        
        
        
        
        recordsTotal = sizeOfRows;
        recordsFiltered = sizeOfRows;
        

        
        
        return data;
    }

    @Override
    public String[][] getTypesOfVariables(String[][] smallDataSet) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        this.smallDataSet = smallDataSet;
        String []str = null;
        String []columnTypes = null;
        typeArr = new String[smallDataSet[0].length][];
        
        for ( int i = 0 ; i < 1 ; i ++){
            columnTypes = new String[smallDataSet[i].length];
            for ( int j = 0 ; j < smallDataSet[i].length ; j ++ ){
                if ( smallDataSet[i][j] != null ){
                    if (smallDataSet[i][j].equals("string")){
                        str = new String[1];
                        str[0] = "string";
                        columnTypes[j] = "string";
                    }
                    else if (smallDataSet[i][j].equals("int")){
                        str = new String[3];
                        str[0] = "int";
                        str[1] = "string";
                        str[2] = "double";
                        columnTypes[j] = "int";
                    }
                    else if (smallDataSet[i][j].equals("date")){
                        str = new String[2];
                        str[0] = "date";
                        str[1] = "string";
                        columnTypes[j] = "date";
                    }
                    else if(smallDataSet[i][j].equals("set")){
                        str = new String[1];
                        str[0] = "set";
                    }
                    else {
                        str = new String[2];
                        str[0] = "double";
                        str[1] = "string";
                        columnTypes[j] = "double";

                    }
                }
                else{
                    str = new String[5];
                    str[0] = "string";
                    str[1] = "int";
                    str[2] = "double";
                    str[3] = "date";
                    str[4] = "set";
                    columnTypes[j] = "string";
                }
                typeArr[j] = str;
            }
            
        
        }

        return typeArr;
    }

    @Override
    public int getRecordsTotal() {
        return recordsTotal;
    }

    @Override
    public Map<Integer, String> getColNamesType() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return colNamesType;
    }

    @Override
    public String getInputFile() {
        String delimiter = "/";
        String[] temp = inputFile.split(delimiter,-1);
        return temp[temp.length-1];
    }

    @Override
    public SimpleDateFormat getDateFormat(int column) {
        return new SimpleDateFormat("dd/MM/yyyy");
    }
    
}
