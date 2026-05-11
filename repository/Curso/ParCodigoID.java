
package repository.Curso;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParCodigoID implements repository.RegistroHashExtensivel<ParCodigoID> {

  private String codigo;
  private int id;
  // NanoID de 10 chars: writeUTF = 2 (prefixo) + 10 (dados) = 12 bytes; int = 4 → total = 16 bytes
  private short TAMANHO = 16;

  public ParCodigoID() {
    this("", -1);
  }

  public ParCodigoID(String c, int i) {
    try {
      this.codigo = c;
      this.id = i;
      if (c.getBytes().length + 4 > TAMANHO)
        throw new Exception("Número de caracteres do código maior que o permitido. Os dados serão cortados.");
    } catch (Exception ec) {
      ec.printStackTrace();
    }
  }

  public int getId() {
    return this.id;
  }

  @Override
  public int hashCode() {
    return Math.abs(this.codigo.hashCode());
  }

  public short size() {
    return this.TAMANHO;
  }

  public String toString() {
    return this.codigo + ";" + this.id;
  }

  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeUTF(codigo);
    dos.writeInt(id);
    byte[] bs = baos.toByteArray();
    byte[] bs2 = new byte[TAMANHO];
    for (int i = 0; i < TAMANHO; i++)
      bs2[i] = ' ';
    for (int i = 0; i < bs.length && i < TAMANHO; i++)
      bs2[i] = bs[i];
    return bs2;
  }

  public void fromByteArray(byte[] ba) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba);
    DataInputStream dis = new DataInputStream(bais);
    this.codigo = dis.readUTF();
    this.id = dis.readInt();
  }

  public static int hash(String codigo) {
    return Math.abs(codigo.hashCode());
  }

}
