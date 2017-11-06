package br.edu.opet.Crud;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import br.edu.opet.Util.Leitor;
import redis.clients.jedis.Jedis;

// Inserindo os dados em uma estrutura do tipo "string"
// HSET Usuario:ZE Nome "José da silva"
// HSET Usuario:ZE Apelido "ZE"
// HSET Usuario:ZE DataCadastro "20/10/2017"
/*
 * SET ZE:20/10/2017:09:27:45 "Ai Jesuis" 
 * SADD ZE:20/10/2017:09:27:45:RECEIVER "maria" "nemeu" 
 * ZADD JULIA:ENTRADA 1
 * 
 * 
 * ZADD ZE:20102017092745:RESPOSTAS 1 "NEMEU:20102017092855" 
 * ZADD ZE:20102017092745:RESPOSTAS 2 "MARIA:20102017093030"
 * 
 * INCR ZE:20102017092745:COUNT
 * 
 * BUSCA-- ZRANGE ZE:20102017092745:RESPOSTAS 0 -1
 * //Ola gostaria de te convidar para minha festa de aniversario dia 05/11/2017 as 17:00. Favor confirmar presenca. Ate logo."
 */

public class RedisJava {

	private static DateTimeFormatter sFormatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static DateTimeFormatter sFormatadorHora = DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss");

	public static void main(String[] args) {
		// Conectando no Redis server - localhost
		Jedis jedis = new Jedis("localhost");

		enquanto1: while (true) {
			// Menu Iniciar

			System.out.println();
			System.out.println("1 - Criar Novo Usuário");
			System.out.println("2 - Entrar");
			System.out.println("3 - Excluir Usuário");
			System.out.println("4 - Sair do Programa");

			System.out.println();

			int tMenu = Leitor.readInt("O que você deseja fazer? ");

			casos1: switch (tMenu) {

			case 1:// Criando novo usuario

				LocalDate tDtAtualCad = LocalDate.now();

				System.out.println("Novo Usuário");
				String tNome = Leitor.readString("Nome:");
				if (tNome == "") {
					break casos1;
				}
				while (true) {

					String tApelido = Leitor.readString("Apelido:");
					if (tApelido == "") {
						break casos1;
					}

					String tApelidoBd1 = jedis.hget("Usuario:" + tApelido, "Apelido");

					if (!(tApelidoBd1 == null)) {
						System.out.println("Este apelido de usuário já está em uso. Tente outro.");
						System.out.println();
					} else {
						String tSenha = Leitor.readString("Senha:");
						if (tSenha == "") {
							break casos1;
						}
						jedis.hset("Usuario:" + tApelido, "Nome", tNome);
						jedis.hset("Usuario:" + tApelido, "Apelido", tApelido);
						jedis.hset("Usuario:" + tApelido, "Senha", tSenha);
						jedis.hset("Usuario:" + tApelido, "DataCadastro", tDtAtualCad.format(sFormatador));
						break casos1;
					}
				}

			case 2:

				while (true) {
					System.out.println();
					System.out.println("LOGIN");
					String tApelidoDigitado = Leitor.readString("Apelido:");
					if (tApelidoDigitado == "") {
						break casos1;
					}

					String tSenhaDigitada = Leitor.readString("Senha:");

					String tApelidoBd = jedis.hget("Usuario:" + tApelidoDigitado, "Apelido");
					String tSenhaBd = jedis.hget("Usuario:" + tApelidoDigitado, "Senha");

					if (tApelidoBd == null || tSenhaBd == null || !tApelidoBd.equals(tApelidoDigitado)
							|| !tSenhaBd.equals(tSenhaDigitada)) {
						System.out.println(" Apelido/Senha inválida");
					} else {
						while (true) {

							System.out.println();
							System.out.println("1 - Enviar Mensagem");
							System.out.println("2 - Caixa de Entradas");
							System.out.println("3 - Caixa de Saída");
							System.out.println("4 - Visualizar Dados");
							System.out.println("100 - Encerrar sessão");
							System.out.println();

							int tMenu2 = Leitor.readInt("O que você desejá fazer? ");

							casos2: switch (tMenu2) {

							case 1:

								LocalDateTime tDtAtualMsg = LocalDateTime.now();

								System.out.println();
								String tPara = Leitor.readString("Para:");

								if (tPara == "") {
									break casos2;
								}

								String tMsg = Leitor.readString("Mensagem:");

								if (tMsg == "") {
									break casos2;
								}

								jedis.sadd(tApelidoDigitado + ":" + tDtAtualMsg.format(sFormatadorHora) + ":Para",
										tPara);
								jedis.set(tApelidoDigitado + ":" + tDtAtualMsg.format(sFormatadorHora) + ":De",
										tApelidoDigitado);
								jedis.set(tApelidoDigitado + ":" + tDtAtualMsg.format(sFormatadorHora) + ":Mensagem",
										tMsg);
								// System.out.println(jedis.smembers(tApelidoDigitado + ":" +
								// tDtAtualMsg.format(sFormatadorHora) + ":Para "));

								Long nSaidas = jedis.incr(tApelidoDigitado + "--saida");
								jedis.zadd(tApelidoDigitado + "--saida ", nSaidas,
										tApelidoDigitado + ":" + tDtAtualMsg.format(sFormatadorHora));

								String[] textoSeparado = tPara.split(",");
								// System.out.println(Arrays.toString(textoSeparado));
								for (int i = 0; i < textoSeparado.length; i++) {
									// System.out.println(textoSeparado[i]);

									Long nEntradas = jedis.incr(textoSeparado[i] + "--entr ");

									jedis.zadd(textoSeparado[i] + "--entr  ", nEntradas,
											tApelidoDigitado + ":" + tDtAtualMsg.format(sFormatadorHora));
								}
								break casos2;

							case 2:

								System.out.println("Principais");

								Long contadorentradas = jedis.zlexcount(tApelidoDigitado + "--entr  ", "-", "+");
								System.out.println("Você tem " + contadorentradas + " mensagens.");

								if (contadorentradas == 0) {

									break casos2;
								} else {
									for (int i = 0; i < contadorentradas; i++) {

										System.out.println(
												(1 + i) + " " + jedis.zrange(tApelidoDigitado + "--entr  ", i, i));

									}

									System.out.println();
									Long tVisualisar = Leitor.readLong("Visualisar mensagem:");

									if (tVisualisar == 0) {
										break casos2;
									} else {

										Set<String> tMsgV = jedis.zrange(tApelidoDigitado + "--entr  ", tVisualisar - 1,
												tVisualisar - 1);

										String stringCortando = (tMsgV).toString();
										String tMsgV2 = stringCortando.substring(1, stringCortando.length() - 1);

										System.out.println(
												jedis.get(tMsgV2 + ":De") + ": " + jedis.get(tMsgV2 + ":Mensagem"));
										System.out.println();

										String tResp = Leitor.readString("Resposta:");

										if (tResp == "") {
											break casos2;
										} else {
											LocalDateTime tDtAtualMsg2 = LocalDateTime.now();

											Long nEntradasResp = jedis.incr(tMsgV2 + "--respo  ");
											jedis.zadd(tMsgV2 + "--respo ", nEntradasResp,
													tApelidoDigitado + ":" + tDtAtualMsg2.format(sFormatadorHora));
											String tPara2 = jedis.get(tMsgV2 + ":De");

											jedis.sadd(tApelidoDigitado + ":" + tDtAtualMsg2.format(sFormatadorHora)
													+ ":Para", tPara2);
											jedis.set(tApelidoDigitado + ":" + tDtAtualMsg2.format(sFormatadorHora)
													+ ":De", tApelidoDigitado);
											jedis.set(tApelidoDigitado + ":" + tDtAtualMsg2.format(sFormatadorHora)
													+ ":Mensagem", tResp);

											Long nSaidas2 = jedis.incr(tApelidoDigitado + "--saida");
											jedis.zadd(tApelidoDigitado + "--saida ", nSaidas2,
													tApelidoDigitado + ":" + tDtAtualMsg2.format(sFormatadorHora));

											Long nEntradas2 = jedis.incr(tPara2 + "--entr ");

											jedis.zadd(tPara2 + "--entr  ", nEntradas2,
													tApelidoDigitado + ":" + tDtAtualMsg2.format(sFormatadorHora));

										}
										/* ZADD ZE:20102017092745:RESPOSTAS 1 "NEMEU:20102017092855" */
									}
								}
								break casos2;

							case 3:

								System.out.println("Principais");

								Long contadorsaidas = jedis.zlexcount(tApelidoDigitado + "--saida ", "-", "+");

								System.out.println("Você enviou " + contadorsaidas + " mensagens.");

								if (contadorsaidas == 0) {

									break casos2;
								} else {

									for (int i = 0; i < contadorsaidas; i++) {

										System.out.println(
												(1 + i) + " " + jedis.zrange(tApelidoDigitado + "--saida ", i, i));
									}

									enquanto9: while (true) {

										System.out.println();
										Long tVisualisarSaida = Leitor.readLong("Visualisar mensagem:");

										if (tVisualisarSaida == 0 || tVisualisarSaida < 0) {
											break casos2;
										} else {

											Set<String> tMsgV3 = jedis.zrange(tApelidoDigitado + "--saida ",
													tVisualisarSaida - 1, tVisualisarSaida - 1);

											String stringCortando = (tMsgV3).toString();
											String tMsgV4 = stringCortando.substring(1, stringCortando.length() - 1);

											System.out.println("Para: " + jedis.smembers(tMsgV4 + ":Para"));
											System.out.println(jedis.get(tMsgV4 + ":Mensagem"));

											Long contadorrespostas = jedis.zlexcount(tMsgV4 + "--respo ", "-", "+");
											System.out.println();
											System.out.println("Você possui " + contadorrespostas + " respostas.");
											
											
											if (contadorrespostas == 0) {
												break enquanto9;
											} else {
												System.out.println("Respostas:");
												// para mostrar os nomes das pessoas que enviaram as respostas
												for (int i = 0; i < contadorrespostas; i++) {
													Set<String> tMsgV5 = jedis.zrange(tMsgV4 + "--respo ", i, i);

													String stringCortando2 = (tMsgV5).toString();
													String tTirandochave = stringCortando2.substring(1,
															stringCortando2.length() - 1);

													String tNome2 = jedis.get(tTirandochave + ":De");

													System.out.println((1+i) + "- " + tNome2);
												}

												System.out.println();
												Long tVisualisarResposta = Leitor.readLong("Visualisar mensagem:");

												if (tVisualisarResposta == 0 || tVisualisarResposta < 0) {
													break casos2;
												} else {

													Set<String> tMsgV7 = jedis.zrange(tMsgV4 + "--respo ",
															tVisualisarResposta - 1, tVisualisarResposta - 1);
													String stringCortando3 = (tMsgV7).toString();
													String tTirandochave2 = stringCortando3.substring(1,
															stringCortando3.length() - 1);

													System.out.println(jedis.get(tTirandochave2 + ":Mensagem"));

													String opf = Leitor.readString("Enter para Sair!");
													if (opf == "")
														break enquanto9;
												}
											}
										}
									}
								}
								break casos2;

							case 4:
								System.out.println();
								System.out.println("Visualizar os Dados");

								System.out.println("Apelido: " + jedis.hget("Usuario:" + tApelidoDigitado, "Apelido"));
								System.out.println("Nome: " + jedis.hget("Usuario:" + tApelidoDigitado, "Nome"));
								System.out.println("Data de Cadastro: "
										+ jedis.hget("Usuario:" + tApelidoDigitado, "DataCadastro"));
								System.out.println();
								break casos2;

							case 100:
								break casos1;
							}
						}
					}
				}

			case 3:

				System.out.println("Digite o apelido que deseja excluir.");
				System.out.println();
				String tApelidoDel = Leitor.readString("Apelido:");
				jedis.del("Usuario:" + tApelidoDel);
				jedis.del(tApelidoDel + "entr");
				jedis.del(tApelidoDel + "saida");

				String tRetorno = Leitor.readString(jedis.hget("Usuario:" + tApelidoDel, "Nome"));
				if (tRetorno == null) {
					System.out.println("Usuário excluido com sucesso.");
				}
				break;

			case 4:
				break enquanto1;
			}

		}
		jedis.close();
	}
}