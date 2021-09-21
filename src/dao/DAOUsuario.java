package dao;

import modelo.Mensagem;
import modelo.Usuario;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

public class DAOUsuario extends DAO<Usuario>{


    public Usuario readByNome(Object chave) {
        try{
            String dados = (String) chave;
            String[] separaDados = dados.split("/");
            String nome = separaDados[0];

            TypedQuery<Usuario> q = manager.createQuery("select u from Usuario u where u.nomesenha like CONCAT(:n,'%')", Usuario.class)
                    .setParameter("n", nome);
            return q.getResultList().get(0);
        }catch(NoResultException e){
            return null;
        }
    }

    public Usuario read(Object chave) {
        try{
            String nomesenha = (String) chave;

            TypedQuery<Usuario> q = manager.createQuery("select u from Usuario u where u.nomesenha=:n", Usuario.class);
            q.setParameter("n", nomesenha);
            return q.getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }

    public List<Usuario> readAll() {
        TypedQuery<Usuario> q = manager.createQuery("select u from Usuario u order by u.id", Usuario.class);
        return  q.getResultList();
    }

}
