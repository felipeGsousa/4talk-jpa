package dao;

import modelo.Mensagem;
import modelo.Usuario;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

public class DAOMensagem extends DAO<Mensagem>{

    public Mensagem read(Object chave) {
        return null;
    }

    public List<Mensagem> readAll() {
        TypedQuery<Mensagem> q = manager.createQuery("select m from Mensagem m order by m.id", Mensagem.class);
        return  q.getResultList();
    }

    public List<Mensagem> getMensagensUsuario(Usuario criador) {
        List<Mensagem> mensagens = readAll();
        List<Mensagem> resultados = new ArrayList<>();
        if (mensagens.size() > 0) {
            for (Mensagem mensagem : mensagens) {
                if (mensagem.getCriador().getNome().equals(criador.getNome())) {
                    resultados.add(mensagem);
                }
            }
        }
        return resultados;
    }
    public List<Mensagem> readByTermo(Object chave) {
        try{
            String termo = (String) chave;
            TypedQuery<Mensagem> q = manager.createQuery("select m from Mensagem m where LOWER(m.texto) like CONCAT('%',:termo,'%')", Mensagem.class)
                    .setParameter("termo", termo);
            return q.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }
}
