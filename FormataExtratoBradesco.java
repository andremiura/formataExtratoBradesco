import java.io.*;

public class FormataExtratoBradesco {
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

                String data = registro[0];
                String descricao = registro[1];
                String codigo = registro[2];
                String credito = registro[3];
                String debito = registro[4];

                System.out.println( data + ";" + descricao + ";" + codigo + ";"  + credito + ";" + debito + ";" + ";");
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
    }//main
}//class
