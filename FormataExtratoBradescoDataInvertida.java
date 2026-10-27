import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FormataExtratoBradescoDataInvertida {
    /**
     * @param args
     */
    public static void main(String[] args) {


        // The name of the file to open.
        String fileName = null;

        int numRegistro=0;
        try {
            if( args.length >= 1)

                // The name of the file to open.
                fileName = args[0];
            else fileName = "";


            for (String[] registro : ExtratoBradesco.le(fileName)) {

                String[] data = registro[0].split("/");
                String dia = data[0];
                String mes = data[1];
                String ano = data[2];

                String descricao = registro[1];
                String codigo = registro[2];
                String credito = registro[3];
                String debito = registro[4];

                System.out.print( "20" + ano + "-" + mes + "-" + dia + ";" + descricao + ";" + codigo + ";"  + credito + ";" + debito + ";" + ";");

                String registroConcatenado = dia + "/" + mes + "/" + ano  + descricao + codigo + credito + debito;

                //System.out.print(registroConcatenado);



                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(registroConcatenado.getBytes());
                byte[] digest = md.digest();
                StringBuffer hexString = new StringBuffer();
                for (int i = 0; i < digest.length; i++) {
                    hexString.append(Integer.toHexString(0xFF & digest[i]));
                }

                System.out.println(hexString.toString() + ";");

                numRegistro++;
            }
            //System.out.println("\n\n numero de registros: " + numRegistro);
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
