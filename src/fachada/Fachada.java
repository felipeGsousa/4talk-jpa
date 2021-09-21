package fachada;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import dao.DAO;
import dao.DAOLog;
import dao.DAOMensagem;
import dao.DAOUsuario;
import modelo.Administrador;
import modelo.Log;
import modelo.Mensagem;
import modelo.Usuario;

public class Fachada {
	private static DAOUsuario daousuario = new DAOUsuario();
	private static DAOMensagem daomensagem = new DAOMensagem();
	private static DAOLog daolog = new DAOLog();

	private static Usuario usuariologado=null;


	public static void inicializar() {
		DAO.open();
	}

	public static void finalizar(){
		DAO.close();
	}

	public static List<Usuario> listarUsuarios() {
		// nao precisa estar logado
		return daousuario.readAll();	
	}
	public static List<Mensagem> listarMensagens() {
		// nao precisa estar logado
		return daomensagem.readAll();
	}

	public static List<Log> listarLogs() {
		// nao precisa estar logado
		return daolog.readAll();	
	}
	public static List<Mensagem> buscarMensagens(String termo) throws  Exception{
		/*
		 * nao precisa estar logado
		 * query no banco para obter mensagens do grupo que contenha
		 *  o termo (considerar case insensitive)
		 * 
		 */
		termo = termo.toLowerCase();
		return daomensagem.readByTermo(termo);
	}

	public static Usuario criarUsuario(String nome, String senha) throws  Exception{
		// nao precisa estar logado
		DAO.begin();	
		Usuario u = daousuario.read(nome+"/"+senha);	
		if(u != null) {
			DAO.rollback();	
			throw new Exception("criar usuario - usuario existente:" + nome);
		}

		u = new Usuario(nome+"/"+senha);
		daousuario.create(u);
		DAO.commit();
		return u;
	}


	public static void login(String nome, String senha) throws Exception{		
		//verificar se ja existe um usuario logada
		if(usuariologado!=null)
			throw new Exception ("ja existe um usuario logado"+getNomeLogado());

		DAO.begin();	
		Usuario u = daousuario.read(nome+"/"+senha);
		if(u == null) {
			DAO.rollback();	
			throw new Exception("login - usuario inexistente:" + nome);
		}
		if(!u.ativo()) {
			DAO.rollback();
			throw new Exception("login - usuario nao ativo:" + nome);
		}
		usuariologado = u;		//altera o logado na fachada
		Log log = new Log(usuariologado.getNome() + " - login");
		daolog.create(log);
		DAO.commit();
	}

	private static String getNomeLogado() {
		return usuariologado.getNome();
	}

	public static void logoff() {
		DAO.begin();
		Log log = new Log(usuariologado.getNome() + " - logoff");
		daolog.create(log);
		DAO.commit();

		usuariologado = null; 		//altera o logado na fachada
	}

	public static Usuario getLogado() {
		return usuariologado;
	}

	public static Mensagem criarMensagem(String texto) throws Exception{
		/*
		 * tem que esta logado
		 * criar a mensagem, onde o criador � a usuario logada
		 * adicionar esta mensagem na lista de mensagens de cada usuario do grupo,
		 * incluindo a do criador
		 * retornar mensagem criada
		 */

		//para gerar o novo id da mensagem utilize:
		//		int id = daomensagem.obterUltimoId();
		//		id++;
		//		Mensagem m = new Mensagem(id, usuariologado, texto);

		DAO.begin();
		if(usuariologado != null) {
			Mensagem m = new Mensagem(usuariologado, texto);
			daomensagem.create(m);
			usuariologado.adicionar(m);
			Log log = new Log(usuariologado.getNome() + " - envia mensagem");
			daolog.create(log);
			DAO.commit();
			return m;
		}
		DAO.rollback();
		throw new Exception("É preciso estar logado para enviar uma mensagem.");
	}

	public static List<Mensagem> listarMensagensUsuario() throws Exception{
		/*
		 * tem que esta logado
		 * retorna todas as mensagens do usuario logado
		 * 
		 */
		if(usuariologado != null){
			return daomensagem.getMensagensUsuario(usuariologado);
		}
		throw new Exception("É necessário estar logado para visualizar as mensagens");
	}

	public static void apagarMensagens(int... ids) throws  Exception{
		/*
		 * tem que esta logado
		 * recebe uma lista de numeros de id 
		 * (id � um numero entre 1 a N, onde N � a quatidade atual de mensagens do grupo)
		 * validar se ids s�o de mensagens criadas pelo usuario logado
		 * (um usuario nao pode apagar mensagens de outros usuarios)
		 * 
		 * remover cada mensagem da lista de mensagens do usuario logado
		 * apagar cada mensagem do banco 
		 */

		if(ids.length<1){
			throw new Exception("Nenhum ID informado, por favor insira um número maior que 0");
		}
		DAO.begin();
		if(usuariologado != null){
			List<Mensagem> mensagens = listarMensagens();
			List<Mensagem> paraApagar = new ArrayList<>();

			for(Mensagem mensagem:mensagens) {
				for (int id : ids) {
					if (mensagem.getId() == id && usuariologado.getNome().equals(mensagem.getCriador().getNome())) {
						paraApagar.add(mensagem);
					}
				}
			}
			if(paraApagar.size()>0){
				for(Mensagem mensagem : paraApagar){
					usuariologado.remover(mensagem);
					daomensagem.delete(mensagem);
				}
			}else {
				DAO.rollback();
				throw new Exception("As mensagens pertencem a outro usuário");
			}
			Log log = new Log(usuariologado.getNome() + " - apaga mensagem");
			daolog.create(log);
			DAO.commit();
		}else {
			DAO.rollback();
			throw new Exception("É necessário estar logado para apagar mensagens");
		}
	}

	public static void sairDoGrupo() throws  Exception{

	    if(usuariologado==null){
			throw new Exception("É necessário estar logado para sair do grupo");
        }
		usuariologado.desativar();
		criarMensagem(usuariologado.getNome() + " saiu do grupo");
		Log log = new Log(usuariologado.getNome() + " - saiu do grupo");
		daolog.create(log);
		daousuario.update(usuariologado);
		logoff();
		/*
		 * tem que esta logado
		 * 
		 * criar a mensagem "fulano saiu do grupo"
		 * desativar o usuario logado e fazer logoff dele
		 */
	}

	//	public static int totalMensagensUsuario() throws Exception{
	//		/*
	//		 * tem que esta logado
	//		 * retorna total de mensagens criadas pelo usuario logado
	//		 * 
	//		 */
	//	}

	public static void esvaziar() throws Exception{
		DAO.clear();
	}

	/**************************************************************
	 * 
	 * NOVOS M�TODOS DA FACHADA PARA O PROJETO 2
	 * 
	 **************************************************************/

	public static Administrador criarAdministrador(String nome, String senha, String email) throws  Exception{
		// nao precisa estar logado
		DAO.begin();	
		Usuario u = daousuario.read(nome+"/"+senha);	
		if(u != null) {
			DAO.rollback();	
			throw new Exception("criar administrador - usuario ja existe:" + nome);
		}

		Administrador ad = new Administrador(nome+"/"+senha, email);
		daousuario.create(ad);		
		DAO.commit();
		return ad;
	}

	public static void solicitarAtivacao(String nome, String senha) throws  Exception{
		/*
		 * o usuario (nome+senha) tem que estar desativado
		 *  
		 * enviar um email para o administrador com a mensagem "nome solicita ativa��o"
		 * usar o m�todo Fachada.enviarEmail(...) 
		 * 
		 */
		Usuario usuario = daousuario.read(nome+'/'+senha);
		if(!usuario.ativo()){
			enviarEmail("Reativação de conta",nome + " solicita ativação");
		}
	}

	public static void solicitarExclusao(String nome, String senha) throws  Exception{
		/*
		 * o usuario (nome+senha) tem que estar desativado
		 *  
		 * enviar um email para o administrador com a mensagem "nome solicita exclus�o"
		 * usar o m�todo Fachada.enviarEmail(...) 
		 * 
		 */
		Usuario usuario = daousuario.read(nome+'/'+senha);
		if(!usuario.ativo()){
			enviarEmail("Exclusão de conta",nome + " solicita exclusão");
		}
	}

	public static void ativarUsuario(String nome) throws  Exception{
		/*
		 * o usuario logado tem que ser um administrador  e o usuario a 
		 * ser ativado (nome) tem que estar desativado 
		 *  
		 * ativar o usuario 
		 * criar a mensagem "nome entrou no grupo"
		 * 
		 */
		DAO.begin();
		if(usuariologado != null && usuariologado.getClass().getName().equals("modelo.Administrador")) {
			Usuario usuario = daousuario.readByNome(nome);
			usuario.ativar();
			daousuario.update(usuario);
			Log log = new Log(usuariologado.getNome() + " - ativa usuário");
			daolog.create(log);
			DAO.commit();
		}
		DAO.rollback();
	}

	public static void apagarUsuario(String nome) throws  Exception{
		/*
		 * o usuario logado tem que ser um administrador  e o usuario a 
		 * ser apagado tem que estar desativado (e n�o pode ser do tipo Administrador)
		 *  
		 * apagar as mensagens do usuario e apagar o usuario 
		 * criar a mensagem "nome foi excluido do sistema"
		 */
		DAO.begin();
		if(usuariologado != null && usuariologado.getClass().getName().equals("modelo.Administrador")){
			Usuario usuario = daousuario.readByNome(nome);
			if (usuario!=null && !usuario.ativo()) {

				List<Mensagem> mensagensUsuario = daomensagem.getMensagensUsuario(usuario);
				List<Mensagem> mensagens = daomensagem.readAll();
				List<Usuario> usuarios = daousuario.readAll();
				if(mensagensUsuario.size()>0){
					for(int i = 0; i < mensagensUsuario.size();i++){
						mensagens.remove(mensagensUsuario.get(i));
						daomensagem.delete(mensagensUsuario.get(i));
					}
				}
				usuarios.remove(usuario);
				daousuario.delete(usuario);
				Log log = new Log(usuariologado.getNome() + " - apaga usuário");
				daolog.create(log);
				DAO.commit();
			}else{
				DAO.rollback();
				throw new Exception("O usuário precisa estar desativado");
			}
		}else{
			DAO.rollback();
			throw new Exception("O usuário logado precisa ser Administrador");
		}
	}


	/**************************************************************
	 * 
	 * M�TODO PARA ENVIAR EMAIL, USANDO UMA CONTA (SMTP) DO GMAIL
	 * ELE ABRE UMA JANELA PARA PEDIR A SENHA DO EMAIL DO EMITENTE
	 * ELE USA A BIBLIOTECA JAVAMAIL (ver pom.xml)
	 * Lembrar de: 
	 * 1. desligar antivirus e de 
	 * 2. ativar opcao "Acesso a App menos seguro" na conta do gmail
	 * 
	 **************************************************************/
	public static void enviarEmail(String assunto, String mensagem) {
		try {
			/*
			 * ********************************************************
			 * Obs: lembrar de desligar antivirus e 
			 * de ativar "Acesso a App menos seguro" na conta do gmail
			 * 
			 * pom.xml contem a dependencia javax.mail
			 * 
			 * ********************************************************
			 */
			//configurar emails
			String emailorigem = "sousa.felipe@academico.ifpb.edu.br";
			String senhaorigem = pegarSenha();
			String emaildestino = "sousa.felipe@academico.ifpb.edu.br";

			//Gmail
			Properties props = new Properties();
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");
			props.put("mail.smtp.auth", "true");

			Session session;
			session = Session.getInstance(props,
					new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(emailorigem, senhaorigem);
				}
			});

			MimeMessage message = new MimeMessage(session);
			message.setSubject(assunto);		
			message.setFrom(new InternetAddress(emailorigem));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emaildestino));
			message.setText(mensagem);   // usar "\n" para quebrar linhas
			Transport.send(message);

			//System.out.println("enviado com sucesso");

		} catch (MessagingException e) {
			System.out.println(e.getMessage());
		}
	}
	
	/*
	 * JANELA PARA DIGITAR A SENHA DO EMAIL
	 */
	public static String pegarSenha(){
		JPasswordField field = new JPasswordField(10);
		field.setEchoChar('*'); 
		JPanel painel = new JPanel();
		painel.add(new JLabel("Entre com a senha do email:"));
		painel.add(field);
		JOptionPane.showMessageDialog(null, painel, "Senha", JOptionPane.PLAIN_MESSAGE);
		String texto = new String(field.getPassword());
		return texto.trim();
	}
}

