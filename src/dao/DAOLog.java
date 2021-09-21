package dao;

import modelo.Log;
import modelo.Usuario;

import javax.persistence.TypedQuery;
import java.util.List;

public class DAOLog extends DAO<Log>{

    public Log read(Object chave) {
        return null;
    }

    public List<Log> readAll() {
        TypedQuery<Log> q = manager.createQuery("select l from Log l order by l.id", Log.class);
        return  q.getResultList();
    }
}
