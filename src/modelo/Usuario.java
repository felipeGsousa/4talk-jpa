
package modelo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Cacheable(false)
public class Usuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String nomesenha;			//  nome + / + senha
	private List<Mensagem> mensagens = new ArrayList<>();   //criadas por ele
	private boolean ativo = true;

	public Usuario(String nomesenha) {
		this.nomesenha = nomesenha;
	}

	@Deprecated
	public Usuario(){
	}

	public String getNome() {
		return nomesenha.split("/")[0];
	}
	public void setNome(String nome) {
		this.nomesenha = nome;
	}

	public boolean ativo() {
		return ativo;
	}
	
	public void desativar() {
		ativo=false;
	}

	public void ativar() {
		ativo=true;
	}

	public List<Mensagem> getMensagens() {
		return mensagens;
	}

	public void adicionar(Mensagem me){
		mensagens.add(me);
	}
	public void remover(Mensagem me){
		mensagens.remove(me);
	}
		

	@Override
	public String toString() {
		String texto = "Nome=" + nomesenha ;

		texto += "\n  lista de Mensagens: ";
		if (mensagens.isEmpty())
			texto += "sem mensagens";
		else 	
			for(Mensagem m: mensagens) 
				texto += "\n  --> " + m ;

		return texto ;

	}

}