package modelo;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Cacheable(false)
public class Log {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String datahora;
	private String nome;


	public Log(String nome) {
		this.nome = nome;
		this.datahora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyMMdd HH:mm:ss"));
	}

	@Deprecated
	public Log(){
	}

	

	public String getDatahora() {
		return datahora;
	}



	public String getNome() {
		return nome;
	}



	@Override
	public String toString() {
		return "Log [datahora=" + datahora + ", nome=" + nome + "]";
	}
	
	
	
	
}
