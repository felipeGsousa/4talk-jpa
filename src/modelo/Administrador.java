
package modelo;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

@Entity
@Cacheable(false)
public class Administrador extends Usuario {

	private String email;			

	public Administrador(String nomesenha, String email) {
		super(nomesenha);
		this.email = email;
	}

	@Deprecated
	public Administrador(){
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		String texto = "Nome=" + getNome().split("/")[0] + ", email=" + email;

		texto += "\n  lista de Mensagens: ";
		if (getMensagens().isEmpty())
			texto += "sem mensagens";
		else 	
			for(Mensagem m: getMensagens()) 
				texto += "\n  --> " + m ;

		return texto ;

	}
}