import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FormataExtratoBradescoMD5 {
    /**
     * @param args
     */
    public static void main(String[] args) {

        
        // The name of the file to open.
        String fileName = null;

        
        // This will reference one line at a time
        String line = null;

        String[] campos = null;

                    String data = null;
                    String descricao = null;
                    String codigo = null;
                    String credito = null;
                    String debito = null;
                    String descricaoAnterior = null;
                    String saldo = null;
                    int numRegistro=0;
        try {

            
            if( args.length >= 1)
                
                // The name of the file to open.
                fileName = args[0];
            else fileName = "";
             

            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);

            
            if((line = bufferedReader.readLine()) != null ){

                do{
                       
                       campos = line.split(";");
                    
                    
                        if(campos.length != 2){
                        data = campos[0];
                        descricao = campos[1];
                        codigo = campos[2];
                        credito = campos[3];
                        //saldo = campos[5]; 
                        debito = "0";   
                        }
                    
                        if( campos.length > 4 )
                            debito = campos[4];

                    
                        if((line = bufferedReader.readLine()) != null ){

                            campos = line.split(";");
                        
                            if( campos.length == 2 ){
                            
                                descricao = descricao.concat(" " + campos[1]);
                                line = bufferedReader.readLine();
                            }
                        }

                        String registroCompletoCSV = data + ";" + descricao + ";" + codigo + ";"  + credito + ";" + debito + ";" + ";";
                        System.out.print(registroCompletoCSV);

                        String registroConcatenado = data + descricao + codigo + credito + debito;



                        MessageDigest md = MessageDigest.getInstance("MD5");
                        md.update(registroConcatenado.getBytes());
                        byte[] digest = md.digest();
                        StringBuffer hexString = new StringBuffer();
                        for (int i = 0; i < digest.length; i++) {
                            hexString.append(Integer.toHexString(0xFF & digest[i]));
                        }
                    
                        System.out.println(hexString.toString() + ";");


                        numRegistro++;
                
                } while( line != null);
                //System.out.println("\n\n numero de registros: " + numRegistro);                


            } //end if                       

      
            

            // Always close files.
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Não foi possível abrir o arquivo '" + 
                fileName + "'"); 
            System.out.println(
                "uso: java FormataExtratoBradesco extrato.csv"
            );                   
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");                  
            // Or we could just do this: 
            // ex.printStackTrace();
        }
        catch(NoSuchAlgorithmException ex){

            System.out.println("erro algoritmo");
        }
    }//main
}//class 
