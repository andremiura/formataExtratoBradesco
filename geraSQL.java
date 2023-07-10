import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class geraSQL {
    /**
     * @param args
     */

    public static String calculateSHA256(String input) {
        try {
            // Obter instância do MessageDigest para SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Calcular o hash da string de entrada
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Converter o array de bytes para uma representação hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }// calculateSHA256

    public static String invertDate( String[] data){

        String dia = data[0];
        String mes = data[1];
        String ano = data[2];

        String dataInvertida = "20"+ ano +"-"+mes+"-"+dia;

        return dataInvertida;

    } //invertDate


     public static void main(String[] args) {

        
        // The name of the file to open.
        String fileName = null;

        
        // This will reference one line at a time
        String line = null;

        String[] campos = null;

                    String[] data = null;
                    String dia = null;
                    String mes = null;
                    String ano = null;
                    String descricao = null;
                    String codigo = null;
                    String credito = null;
                    String debito = null;
                    String descricaoAnterior = null;
                    String saldo = null;
                    String hash256 = null;
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
                        
                        data = campos[0].split("/");
                        

                        descricao = campos[1];
                        codigo = campos[2];
                        credito = campos[3].replace(".","").replace(",",".");
                        if(credito.length() == 0) credito = "0"; 
                        //saldo = campos[5]; 
                        debito = "0";   
                        }
                    
                        if( campos.length > 4 ){
                            debito = campos[4].replace(".","").replace(",",".");
                            if(debito.length() == 0) debito = "0";

                        }    
                            

                    
                        if((line = bufferedReader.readLine()) != null ){

                            campos = line.split(";");
                        
                            if( campos.length == 2 ){
                            
                                descricao = descricao.concat(" " + campos[1]);
                                line = bufferedReader.readLine();
                            }
                        }


                        String dataInvertida = invertDate(data);
                        String registroConcatenado = dataInvertida  + descricao + codigo + credito + debito;

                        hash256 = calculateSHA256(registroConcatenado);
                        
                        //System.out.println(registroConcatenado);

                
                        String sql = "INSERT INTO bank_statements (date,historic, code, credit, debit, sha256, created_at,updated_at) VALUES ('"+dataInvertida+"','"+descricao+"','"+codigo+"','"+credito+"','"+debito+"','"+hash256+"',now(),now());";
                        
                        System.out.println(sql);

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
        
    }//main
}//class 
