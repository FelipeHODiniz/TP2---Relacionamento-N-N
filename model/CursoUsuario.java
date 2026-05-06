package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import repository.Registro;

public class CursoUsuario implements Registro {
    private int id;
    private int idCurso;
    private int idUsuario;
    private String dataInscricao;

    public CursoUsuario() {
        this(-1, -1, -1, "");
    }

    public CursoUsuario(int idCurso, int idUsuario, String dataInscricao) {
        this(-1, idCurso, idUsuario, dataInscricao);
    }

    public CursoUsuario(int id, int idCurso, int idUsuario, String dataInscricao) {
        this.id = id;
        this.idCurso = idCurso;
        this.idUsuario = idUsuario;
        this.dataInscricao = dataInscricao;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDataInscricao() {
        return dataInscricao;
    }

    public void setDataInscricao(String dataInscricao) {
        this.dataInscricao = dataInscricao;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeInt(idCurso);
        dos.writeInt(idUsuario);
        dos.writeUTF(dataInscricao);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.idCurso = dis.readInt();
        this.idUsuario = dis.readInt();
        this.dataInscricao = dis.readUTF();
    }
}
