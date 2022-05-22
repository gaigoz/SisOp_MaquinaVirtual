// PUCRS - Escola PolitÃƒÂ©cnica - Sistemas Operacionais
// Prof. Fernando Dotti
// CÃƒÂ³digo fornecido como parte da soluÃƒÂ§ÃƒÂ£o do projeto de Sistemas Operacionais
//
// Fase 1 - mÃƒÂ¡quina virtual (vide enunciado correspondente)
//

import java.util.*;
import java.util.Scanner;

public class Sistema {
	private Interrupt interrupt;

	// -------------------------------------------------------------------------------------------------------
	// --------------------- H A R D W A R E - definicoes de HW
	// ----------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// --------------------- M E M O R I A - definicoes de opcode e palavra de
	// memoria ----------------------

	public class Word { // cada posicao da memoria tem uma instrucao (ou um dado)
		public Opcode opc; //
		public int r1; // indice do primeiro registrador da operacao (Rs ou Rd cfe opcode na tabela)
		public int r2; // indice do segundo registrador da operacao (Rc ou Rs cfe operacao)
		public int p; // parametro para instrucao (k ou A cfe operacao), ou o dado, se opcode = DADO

		public Word(Opcode _opc, int _r1, int _r2, int _p) {
			opc = _opc;
			r1 = _r1;
			r2 = _r2;
			p = _p;
		}
	}
	// -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// --------------------- C P U - definicoes da CPU
	// -----------------------------------------------------

	public enum Opcode {
		DATA, ___, // se memoria nesta posicao tem um dado, usa DATA, se nao usada ee NULO ___
		JMP, JMPIGK, JMPILK, JMPIEK, JMPI, JMPIG, JMPIL, JMPIE, JMPIM, JMPIGM, JMPIGT, JMPILM, JMPIEM, STOP, // desvios
																												// e
																												// parada
		ADDI, SUBI, ADD, SUB, MULT, // matematicos
		LDI, LDD, STD, LDX, STX, SWAP, MOVE, // movimentacao
		TRAP; // chamadas do sistema
	}

	public class CPU {
		// caracterÃƒÂ­stica do processador: contexto da CPU ...
		private int pc; // ... composto de program counter,
		private Word ir; // instruction register,
		private int[] reg; // registradores da CPU
		private Word[] m; // CPU acessa MEMORIA, guarda referencia 'm' a ela. memoria nao muda. ee sempre
							// a mesma.

		public CPU(Word[] _m) { // ref a MEMORIA e interrupt handler passada na criacao da CPU
			m = _m; // usa o atributo 'm' para acessar a memoria.
			reg = new int[10]; // aloca o espaco dos registradores
		}

		public void setContext(int idPrograma) { // no futuro esta funcao vai ter que ser
			//pc = _pc; // limite e pc (deve ser zero nesta versao)
			
			PCB pcbPrograma = vm.gerenteProcessos.getPCBForID(idPrograma);
			int pcbTamPag = pcbPrograma.tamPag;
			int pcPrograma = pcbPrograma.pc;
			
			//pc = vm.gerenteMemoria.pcForFrames;
			pc = pcPrograma;
		}

		private void dump(Word w) {
			System.out.print("[ ");
			System.out.print(w.opc);
			System.out.print(", ");
			System.out.print(w.r1);
			System.out.print(", ");
			System.out.print(w.r2);
			System.out.print(", ");
			System.out.print(w.p);
			System.out.println("  ] ");
		}

		private void showState() {
			System.out.println("       " + pc);
			System.out.print("           ");
			for (int i = 0; i < 10; i++) {
				System.out.print("r" + i);
				System.out.print(": " + reg[i] + "     ");
			}
			;
			System.out.println("");
			System.out.print("           ");
			dump(ir);
		}

		public void run(int idPrograma) { // execucao da CPU supoe que o contexto da CPU, vide acima, esta devidamente
							// setado

			interrupt = Interrupt.NULL;
			
			PCB pcbDesejado = vm.gerenteProcessos.getPCBForID(idPrograma);
			
			System.out.println("============== INICIANDO EXECUCAO DO PROCESSO ==============");
			System.out.println("||");
			
			System.out.println("||");
			System.out.println("||   ########## EXECUTANDO PROGRAMA ##########");
			System.out.println("||");
			
			vm.gerenteProcessos.filaProcessosProntos.remove(pcbDesejado);
			vm.gerenteProcessos.filaProcessosRodando.add(pcbDesejado);
			vm.gerenteProcessos.printaTodasListas();

			while (interrupt == Interrupt.NULL) { // ciclo de instrucoes. acaba cfe instrucao, veja cada caso.
				//int indiceFrameAtual = vm.gerenteMemoria.indexForFrames;
				//int frameDaTabela = vm.gerenteMemoria.tabelaPaginas[indiceFrameAtual];
				
				int indiceFramePCB = pcbDesejado.frameIndex;
				int frameDoPCB = pcbDesejado.tabelaPaginas[indiceFramePCB];
				
				//vm.gerenteProcessos.printaTabelaPaginas(idPrograma);
				
				Frame[] frames = vm.gerenteMemoria.frames;
				
				// printDebugRuntime(indiceFrameAtual, frameDaTabela);
				//printDebugRuntime(indiceFramePCB, frameDoPCB);
				
				//ir = m[pc]; // FETCH - busca posicao da memoria apontada por pc, guarda em ir
				//ir = frames[frameDaTabela].pagina[pc]; // aponta para a primeira pos do primeiro frame, segundo a tabela de frames
				ir = frames[frameDoPCB].pagina[pc]; // aponta para a primeira pos do primeiro frame, segundo a tabela de frames
				
				// if debug
				//showState();
				// EXECUTA INSTRUCAO NO ir
				switch (ir.opc) { // para cada opcode, sua execucao
					// Instrucoes de Memoria
					case LDI: // Rd <- k
						reg[ir.r1] = ir.p;
						pc++;
						checaPC(pc, pcbDesejado);
						
						break;

					case LDD: // Rd <- [A]
						
						if (ir.p >= 0 && ir.p <= 1023) {
							Word wordEfetivaTraduzida = vm.gerenteMemoria.traduzEndereco(ir.p, "AM", pcbDesejado);
							reg[ir.r1] = wordEfetivaTraduzida.p;
							pc++;
							checaPC(pc, pcbDesejado);
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case LDX: // RD <- [RS] // NOVA
						if (reg[ir.r2] >= 0 && reg[ir.r2] <= 1023) {
							Word wordEfetivaTraduzida = vm.gerenteMemoria.traduzEndereco(reg[ir.r2], "AM", pcbDesejado);
							reg[ir.r1] = wordEfetivaTraduzida.p;
							pc++;
							checaPC(pc, pcbDesejado);
						} else {

							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case STD: // [A] <- Rs
						if (ir.p >= 0 && ir.p <= 1023) {
							Word wordEfetivaTraduzida = vm.gerenteMemoria.traduzEndereco(ir.p, "AM", pcbDesejado);
							wordEfetivaTraduzida.opc = Opcode.DATA;
							wordEfetivaTraduzida.p = reg[ir.r1];
							pc++;
							checaPC(pc, pcbDesejado);
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case STX: // [Rd] <- Rs
						if (reg[ir.r1] >= 0 && reg[ir.r1] <= 1023) {
							Word wordEfetivaTraduzida = vm.gerenteMemoria.traduzEndereco(reg[ir.r1], "AM", pcbDesejado);
							wordEfetivaTraduzida.opc = Opcode.DATA;
							wordEfetivaTraduzida.p = reg[ir.r2];
							pc++;
							checaPC(pc, pcbDesejado);
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					// --------------------------------------------------------------------------------------------------
					// --------------------------------------------------------------------------------------------------

					// --------------------------------------------------------------------------------------------------
					// Instrucoes Aritmericas
					case ADDI: // Rd <- Rd + k
						reg[ir.r1] = reg[ir.r1] + ir.p;
						if (reg[ir.r1] >= -10000 && reg[ir.r1] <= 10000) {
							pc++;
							checaPC(pc, pcbDesejado);
						} else {
							interrupt = Interrupt.OVERFLOW;
						}
						break;

					case SUBI: // RD <- RD - k // NOVA
						reg[ir.r1] = reg[ir.r1] - ir.p;
						if (reg[ir.r1] >= -10000 && reg[ir.r1] <= 10000) {
							pc++;
							checaPC(pc, pcbDesejado);
						} else {
							interrupt = Interrupt.OVERFLOW;
						}
						break;

					case ADD: // Rd <- Rd + Rs
						reg[ir.r1] = reg[ir.r1] + reg[ir.r2];
						if (reg[ir.r1] >= -10000 && reg[ir.r1] <= 10000) {
							pc++;
							checaPC(pc, pcbDesejado);
						} else {
							interrupt = Interrupt.OVERFLOW;
						}
						break;

					case SUB: // Rd <- Rd - Rs
						reg[ir.r1] = reg[ir.r1] - reg[ir.r2];
						if (reg[ir.r1] >= -10000 && reg[ir.r1] <= 10000) {
							pc++;
							checaPC(pc, pcbDesejado);
						} else {
							interrupt = Interrupt.OVERFLOW;
						}
						break;

					case MULT: // Rd <- Rd * Rs
						reg[ir.r1] = reg[ir.r1] * reg[ir.r2]; // gera um overflow // --> LIGA INT (1)
						if (reg[ir.r1] >= -10000 && reg[ir.r1] <= 10000) {
							pc++;
							checaPC(pc, pcbDesejado);
						} else {
							interrupt = Interrupt.OVERFLOW;
						}
						break;
					// --------------------------------------------------------------------------------------------------

					// --------------------------------------------------------------------------------------------------
					// Instrucoes JUMP
					case JMP: // PC <- k
						if (ir.p >= 0 && ir.p <= 1023) {
							vm.gerenteMemoria.traduzEndereco(ir.p, "JMP", pcbDesejado);
							//pc = ir.p;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIGK: // If RC > 0 then PC <- k else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r2] > 0) {
								vm.gerenteMemoria.traduzEndereco(ir.p, "JMP", pcbDesejado);
								//pc = ir.p;
							} else {
								pc++;
								checaPC(pc, pcbDesejado);
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPILK: // If RC < 0 then PC <- k else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r2] < 0) {
								vm.gerenteMemoria.traduzEndereco(ir.p, "JMP", pcbDesejado);
								//pc = ir.p;
							} else {
								pc++;
								checaPC(pc, pcbDesejado);
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIEK: // If RC = 0 then PC <- k else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r2] == 0) {
								vm.gerenteMemoria.traduzEndereco(ir.p, "JMP", pcbDesejado);
								//pc = ir.p;
							} else {
								pc++;
								checaPC(pc, pcbDesejado);
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPI: // PC <- Rs
						if (reg[ir.r1] >= 0 && reg[ir.r1] <= 1023) {
							vm.gerenteMemoria.traduzEndereco(reg[ir.r1], "JMP", pcbDesejado);
							//pc = reg[ir.r1];
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIG: // If RC > 0 then PC<-RS else PC++
						if (reg[ir.r1] >= 0 && reg[ir.r1] <= 1023) {
							if (reg[ir.r2] > 0) {
								vm.gerenteMemoria.traduzEndereco(reg[ir.r1], "JMP", pcbDesejado);
								//pc = reg[ir.r1];
							} else {
								pc++;
								checaPC(pc, pcbDesejado);
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIL: // if Rc < 0 then PC <- Rs Else PC <- PC +1
						if (reg[ir.r1] >= 0 && reg[ir.r1] <= 1023) {
							if (reg[ir.r2] < 0) {
								vm.gerenteMemoria.traduzEndereco(reg[ir.r1], "JMP", pcbDesejado);
								//pc = reg[ir.r1];
							} else {
								pc++;
								checaPC(pc, pcbDesejado);
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIE: // If Rc = 0 Then PC <- Rs Else PC <- PC +1
						if (reg[ir.r1] >= 0 && reg[ir.r1] <= 1023) {
							if (reg[ir.r2] == 0) {
								vm.gerenteMemoria.traduzEndereco(reg[ir.r1], "JMP", pcbDesejado);
								//pc = reg[ir.r1];
							} else {
								pc++;
								checaPC(pc, pcbDesejado);
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIM: // PC <- [A]
						if (ir.p >= 0 && ir.p <= 1023) {
							Word wordEfetivaTraduzida = vm.gerenteMemoria.traduzEndereco(ir.p, "AM", pcbDesejado);
							vm.gerenteMemoria.traduzEndereco(wordEfetivaTraduzida.p, "JMP", pcbDesejado);
							//pc = wordEfetivaTraduzida.p;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}

					case JMPIGM: // If RC > 0 then PC <- [A] else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r2] > 0) {
								Word wordEfetivaTraduzida = vm.gerenteMemoria.traduzEndereco(ir.p, "AM", pcbDesejado);
								vm.gerenteMemoria.traduzEndereco(wordEfetivaTraduzida.p, "JMP", pcbDesejado);
								//pc = m[ir.p].p;
							} else {
								pc++;
								checaPC(pc, pcbDesejado);
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPILM: // If RC < 0 then PC <- k else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r2] < 0) {
								Word wordEfetivaTraduzida = vm.gerenteMemoria.traduzEndereco(ir.p, "AM", pcbDesejado);
								vm.gerenteMemoria.traduzEndereco(wordEfetivaTraduzida.p, "JMP", pcbDesejado);
								//pc = m[ir.p].p;
							} else {
								pc++;
								checaPC(pc, pcbDesejado);
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIEM: // If RC = 0 then PC <- k else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r2] == 0) {
								Word wordEfetivaTraduzida = vm.gerenteMemoria.traduzEndereco(ir.p, "AM", pcbDesejado);
								vm.gerenteMemoria.traduzEndereco(wordEfetivaTraduzida.p, "JMP", pcbDesejado);
								//pc = m[ir.p].p;
							} else {
								pc++;
								checaPC(pc, pcbDesejado);
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIGT: // If RS>RC then PC <- k else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r1] > reg[ir.r2]) {
								vm.gerenteMemoria.traduzEndereco(ir.p, "JMP", pcbDesejado);
								//pc = ir.p;
							} else {
								pc++;
								checaPC(pc, pcbDesejado);
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case MOVE: // RD <- RS
						reg[ir.r1] = reg[ir.r2];
						pc++;
						checaPC(pc, pcbDesejado);
						break;

					case STOP:
						interrupt = Interrupt.STOP;
						break;

					// --------------------------------------------------------------------------------------------------
					// --------------------------------------------------------------------------------------------------
					// Chamadas de sistema
					case TRAP:
						chamaSistema();
						break;

					// --------------------------------------------------------------------------------------------------
					// --------------------------------------------------------------------------------------------------

					default: // caso a intrucao nao esteja em nenhum dos cases acima
						interrupt = Interrupt.INSTRUCAO_INVALIDA;
						break;
				}

				// VERIFICA INTERRUPCAO !!! - TERCEIRA FASE DO CICLO DE INSTRUCOES
				trataInterrupcao(interrupt, idPrograma);
			}
		}
		
		public void printDebugRuntime(int indiceFramePCB, int frameDaTabela){
			System.out.println("\n");
			System.out.println("---- DEBUG PAGINACAO EM RUNTIME ----");
			System.out.print("-> Frame atual: ");
			System.out.println(frameDaTabela);
			System.out.println("------------------------------------");
			System.out.print("-> Indice frame atual: ");
			System.out.println(indiceFramePCB);
			System.out.println("------------------------------------");
			System.out.println("\n");
		}
	}
	// ------------------ C P U - fim
	// ------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	// ------------------- V M - constituida de CPU e MEMORIA
	// -----------------------------------------------
	// -------------------------- atributos e construcao da VM
	// -----------------------------------------------
	public class VM {
		public int tamMem;
		public Word[] m;
		public CPU cpu;
		public GerenteDeMemoria gerenteMemoria;
		public GerenteDeProcessos gerenteProcessos;

		public VM() {
			// memoria
			tamMem = 1024;
			m = new Word[tamMem]; // m ee a memoria
			for (int i = 0; i < tamMem; i++) {
				m[i] = new Word(Opcode.___, -1, -1, -1);
			}
	
			// cpu
			cpu = new CPU(m); // cpu acessa memoria
			
			// gerente de memoria
			gerenteMemoria = new GerenteDeMemoria(tamMem);
			
			gerenteProcessos = new GerenteDeProcessos();
		}
	}

	public enum Interrupt {
		NULL, ENDERECO_INVALIDO, INSTRUCAO_INVALIDA, OVERFLOW, STOP
	}

	// ------------------- V M - fim
	// ------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	// --------------------H A R D W A R E - fim
	// -------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------
	// ------------------- S O F T W A R E - inicio
	// ----------------------------------------------------------

	// ------------------------------------------- funcoes de um monitor
	public class Monitor {
				
		public void dump(Word w) {
			System.out.print("[ ");
			System.out.print(w.opc);
			System.out.print(", ");
			System.out.print(w.r1);
			System.out.print(", ");
			System.out.print(w.r2);
			System.out.print(", ");
			System.out.print(w.p);
			System.out.println("  ] ");
		}

		public void dump(Word[] m, int ini, int fim) {
			for (int i = ini; i < fim; i++) {
				System.out.print(i);
				System.out.print(":  ");
				dump(m[i]);
			}
		}

		public void carga(Word[] p, Word[] m) { // significa ler "p" de memoria secundaria e colocar na principal "m"
			for (int i = 0; i < p.length; i++) {
				m[i].opc = p[i].opc;				// carga na antiga memoria, antes do gerente de memoria existir
				m[i].r1 = p[i].r1;
				m[i].r2 = p[i].r2;
				m[i].p = p[i].p;
			}	
		}
		
		public void cargaFrames(Word[] p) {
			int tamPagina = vm.gerenteMemoria.tamPag;
			int tamTabelaPaginas = vm.gerenteMemoria.tamTabelaPaginas;
			int incrementaIndexcomTamanhoPag = 0;
			
			for (int x=0; x<tamTabelaPaginas; x++) {
				int cont = 0;
				int j=0;
			for (int i = incrementaIndexcomTamanhoPag; i<p.length; i++) {
				int frameLidoDaTabela = vm.gerenteMemoria.tabelaPaginas[x];		
				if (cont<tamPagina) {				
					vm.gerenteMemoria.frames[frameLidoDaTabela].pagina[j].opc = p[i].opc;
					vm.gerenteMemoria.frames[frameLidoDaTabela].pagina[j].r1 = p[i].r1;
					vm.gerenteMemoria.frames[frameLidoDaTabela].pagina[j].r2 = p[i].r2;
					vm.gerenteMemoria.frames[frameLidoDaTabela].pagina[j].p = p[i].p;
					cont++;
					j++;
				} else {
					// trocar o frame
					incrementaIndexcomTamanhoPag+=tamPagina;
					break;
				}
			}
				
			}
			System.out.println("||");
			System.out.println("||  ########## PROGRAMA CARREGADO NOS FRAMES ##########");
			System.out.println("||");
		}

		public void executa(int idPrograma) {
			vm.cpu.setContext(idPrograma); // monitor seta contexto - pc aponta para inicio do programa
			vm.cpu.run(idPrograma); // e cpu executa
							// note aqui que o monitor espera que o programa carregado acabe normalmente
							// nao ha protecoes... o que poderia acontecer ?

		}

	}
	
	public class Frame {
		Word[] pagina;
		
		public Frame(int tamPag) {
			pagina = new Word[tamPag];
			for (int x = 0; x < tamPag; x++) {
				pagina[x] = new Word(Opcode.___, -1, -1, -1);
			}
		}
	}

	public class GerenteDeMemoria {				
			int tamMem;			
			int tamPag = 16;
			int tamFrame = tamPag;
			int nroFrames = (tamMem/tamPag);
			
			int pcForFrames = 0;
			int indexForFrames = 0;
			
			Word[] pagina;
						
			Frame[] frames;
			
			boolean[] framesBool; // if TRUE=ocupado, if FALSE=livre
			
			int[] tabelaPaginas;
			int tamTabelaPaginas = 0;
			
			int framesLivres = 0;
			boolean podeAlocar;
			
			int frameTraduzido;
			int pagTraduzida;
						
			public GerenteDeMemoria(int tamMem) { 
					this.tamMem = tamMem;
					pagina = new Word[tamPag];
					nroFrames = (tamMem/tamPag);
					frames = new Frame[nroFrames];
					framesBool = new boolean[nroFrames];
					tabelaPaginas = new int[nroFrames];
					tamTabelaPaginas = 0;
					
					for (int x = 0; x < tamPag; x++) {
						pagina[x] = new Word(Opcode.___, -1, -1, -1);
					}
					
					for (int x = 0; x < nroFrames; x++) {
						frames[x] = new Frame(tamPag);
					}
				
					for (int i = 0; i < nroFrames; i++) {
						framesBool[i] = false;
					}	
					
					for (int i = 0; i < nroFrames; i++) {
						tabelaPaginas[i] = 0;
					}
					
					framesNaoContinuosParaTeste();
			}
			
			public void framesNaoContinuosParaTeste() {
				for (int i = 0; i < 10; i++) {
					framesBool[i] = true;
				}
				
				framesBool[12] = true;
				framesBool[14] = true;
				framesBool[17] = true;
				framesBool[21] = true;
				
				for (int i = 26; i < 30; i++) {
					framesBool[i] = true;
				}
				for (int i = 40; i < 55; i++) {
					framesBool[i] = true;
				}
			}
			
			public int[] aloca(int nroPalavras) {
				
				tabelaPaginas = new int[nroFrames];
				for (int i = 0; i < nroFrames; i++) {
					tabelaPaginas[i] = 0;
				}
				tamTabelaPaginas = 0;
				
				double localNroPalavras = nroPalavras;
				
				double nroPaginas = localNroPalavras/tamPag;
				
				if(nroPaginas>Math.floor(nroPaginas)) {
					// SE FOR UM VALOR QUEBRADO, ARREDONDA PARA CIMA
					nroPaginas = Math.ceil(nroPaginas);
				} else {
					// SE FOR UM VALOR INTEIRO NAO FAZ NADA
				}

				double nroFrames = nroPaginas;
						
				boolean verificaAlocacao = verificaSePodeAlocar(nroFrames);
				podeAlocar = verificaAlocacao;
				if(verificaAlocacao == true) {
					
					for(int i=0; i<nroFrames; i++) {
						
						if(framesBool[i] == true) { // se ocupado avanca no vetor
							nroFrames++;
						} else {
							framesBool[i]=true;	// se livre, ocupa e adiciona na tabela de paginas

							tabelaPaginas[tamTabelaPaginas] = i;
							tamTabelaPaginas++;
						}	
					}
					printaTerminalAlocacao(nroPalavras);
					return tabelaPaginas;
				} else {
					printaTerminalAlocacao(nroPalavras);
					System.exit(0);
					return tabelaPaginas;
				}	
			}
			
			public void desaloca(int[] tabelaPaginas, int tamTabelaPaginas) {
				
				for (int i = 0; i < tamTabelaPaginas; i++) {
					
					framesBool[tabelaPaginas[i]] = false;
					
					for (int j=0; j<tamPag; j++) {
						
						frames[tabelaPaginas[i]].pagina[j] = new Word(Opcode.___, -1, -1, -1);
					}
				}
				
				printaFrameBool();
				
			}
			
			public int contaFramesLivres() {
				int contaFramesLivres = 0;
				for (int i = 0; i < nroFrames; i++) {
					if(framesBool[i] == false) {
						contaFramesLivres++;
						framesLivres = contaFramesLivres;
					}	
				}
				return contaFramesLivres;
			}
			
			public boolean verificaSePodeAlocar(double nroFrames) {
				double framesLivres = contaFramesLivres();
				
				if((framesLivres-nroFrames) > 0) {
					return true;
				} else {
					return false;
				}	
			}
			
			public void printaTerminalAlocacao(int nroPalavras) {
				double localNroPalavras = nroPalavras;
				double nroPaginas = localNroPalavras/tamPag;
				
				if(nroPaginas>Math.floor(nroPaginas)) {
					// SE FOR UM VALOR QUEBRADO, ARREDONDA PARA CIMA
					nroPaginas = Math.ceil(nroPaginas);
				} else {
					// SE FOR UM VALOR INTEIRO NAO FAZ NADA
				}

				double nroFrames = nroPaginas;	
				
				System.out.print("\n");
				System.out.println("============== REQUISICAO DE ALOCACAO ==============");
				
				System.out.println("||");
				System.out.print("|| -> Deseja-se alocar ");
				System.out.print(nroPalavras);
				System.out.println(" palavras;");
				
				System.out.println("||");
				System.out.print("|| -> O tamanho atual da pagina eh de ");
				System.out.print(tamPag);
				System.out.println(" palavras;");
				
				System.out.print("||     -> Logo, precisa-se de ");
				System.out.print(nroFrames);
				System.out.println(" paginas;");
				
				System.out.println("||");
				System.out.println("|| -> Cada frame tem exatamente 01 pagina;");
				System.out.print("||     -> Logo, precisa-se de ");
				System.out.print(nroFrames);
				System.out.println(" frames;");
				
				System.out.println("||");
				System.out.print("|| -> Dos ");
				System.out.print(vm.gerenteMemoria.nroFrames);
				System.out.println(" que o sistema dispoe:");
				
				System.out.print("||     -> ");
				System.out.print(framesLivres);
				System.out.println(" esta(o) livre(s);");
				
				if(podeAlocar==true){
					System.out.println("||");
					System.out.println("|| -> REQUISICAO DE ALOCACAO AUTORIZADA!");
				} else {
					System.out.print("||");
					System.out.println("|| -> REQUISICAO DE ALOCACAO NEGADA");
				}
				
				System.out.println("||");
				System.out.println("|| -> Tabela de Paginas gerada:");
				printaTabelaPaginas(tamTabelaPaginas);
				
				System.out.print("||");
				System.out.println("=============================================");
				System.out.print("\n");
				System.out.print("\n");
			}
			
			public void printaFrameBool() {
				for(int i=0; i<nroFrames ; i++) {
					System.out.print("[");
					System.out.print(i);
					System.out.print("] :");
					System.out.println(framesBool[i]);
				}
			}
 			
			public void printaFrames() {
				System.out.print("\n");
				System.out.println("============== DUMP DE TODOS OS FRAMES DO SISTEMA ==============");
				System.out.println("||");
				for(int i=0; i<frames.length; i++) {
					System.out.print("|| ------------- FRAME[");
					System.out.print(i);
					System.out.println("] -------------");
					for(int j=0; j<tamPag; j++) {
						System.out.print("||   ");
						System.out.print(vm.gerenteMemoria.frames[i].pagina[j].opc); 
						System.out.print("  ");
						System.out.print(vm.gerenteMemoria.frames[i].pagina[j].r1); 
						System.out.print(" ");
						System.out.print(vm.gerenteMemoria.frames[i].pagina[j].r2); 
						System.out.print(" ");
						System.out.println(vm.gerenteMemoria.frames[i].pagina[j].p);
					}
					System.out.println("|| ------------------------------------");
				}
				System.out.println("=============================================");
			}
			
			public void printaFramesDaTabelaPaginas() {
				System.out.println("\n");
				for(int i=0; i<tamTabelaPaginas ; i++) {
					System.out.print("---------------- FRAME[");
					System.out.print(tabelaPaginas[i]);
					System.out.println("] ----------------");
					for(int j=0; j<tamPag; j++) {
						System.out.print("   [");
						System.out.print(j);
						System.out.print("] ");
						System.out.print(vm.gerenteMemoria.frames[tabelaPaginas[i]].pagina[j].opc); 
						System.out.print("  ");
						System.out.print(vm.gerenteMemoria.frames[tabelaPaginas[i]].pagina[j].r1); 
						System.out.print(" ");
						System.out.print(vm.gerenteMemoria.frames[tabelaPaginas[i]].pagina[j].r2); 
						System.out.print(" ");
						System.out.println(vm.gerenteMemoria.frames[tabelaPaginas[i]].pagina[j].p);
					}
					System.out.println("\n");
				}
			}
			
			public void printaTabelaPaginas(int n) {
				System.out.println("||     --------------");
				System.out.println("||     | PG | FRAME |");
				System.out.println("||     --------------");
				for(int i=0; i<n ; i++) {	
					
					System.out.print("||     | ");
					System.out.print(i);
					System.out.print(" |   ");
					System.out.print(tabelaPaginas[i]);
					System.out.println("   |");
					System.out.println("||     --------------");
				}
			}
			
			public Word traduzEndereco(int logicAddress, String opType, PCB pcbPrograma) {
				int p = logicAddress/tamPag;
				int offset = logicAddress%tamPag;
				int tabelaNro = pcbPrograma.tabelaPaginas[p];
				Word wordNaPosEfetiva = vm.gerenteMemoria.frames[tabelaNro].pagina[offset];
				int effectiveAddress = (pcbPrograma.tabelaPaginas[p]*tamFrame) + offset;
				
				setFrameTraduzido(tabelaNro);
				setPagTraduzida(offset);
	
				switch (opType) {	// DAM = "Doesn't Access Memory"
									// AM = "Access Memory"
									// JMP = Jump
				
					case "AM":
						//printDebugTraducao(logicAddress, p, offset, tabelaNro, wordNaPosEfetiva, effectiveAddress);
						return wordNaPosEfetiva;
						
					case "JMP":
						mudaPC(p, offset, pcbPrograma);
						return wordNaPosEfetiva;
						
					default:
						break;
				}
				
				return wordNaPosEfetiva;
			}
			
			public void setFrameTraduzido(int tabelaNro) {
				frameTraduzido = tabelaNro;
			}
			
			public int getFrameTraduzido() {
				return frameTraduzido;	
			}
			
			public void setPagTraduzida(int offset) {
				pagTraduzida = offset;
			}
			
			public int getPagTraduzida() {
				return pagTraduzida;
			}
			
			
			public void printDebugTraducao(int logicAddress, int p, int offset, int tabelaNro, Word wordNaPosEfetiva, int effectiveAddress) {
				System.out.print("\n");
				System.out.println("---- DEBUG TRADUCAO ----");
				System.out.print("-> Endereco a ser traduzido: ");
				System.out.println(logicAddress);
				System.out.print("-> Traduzido para: ");
				System.out.println(effectiveAddress);
				System.out.println("-> Endereco encontra-se: ");
				System.out.print("     -> Frame: ");
				System.out.println(tabelaNro);
				System.out.print("     -> Linha: ");
				System.out.print(offset);
				System.out.println("  (conta a partir de 0)");
				
				System.out.print("-> Conteudo da posicao traduzida: ");
				System.out.print(wordNaPosEfetiva.opc);
				System.out.print(" ");
				System.out.print(wordNaPosEfetiva.r1);
				System.out.print(" ");
				System.out.print(wordNaPosEfetiva.r2);
				System.out.print(" ");
				System.out.println(wordNaPosEfetiva.p);
				System.out.println("------------------------");
				System.out.print("\n");
			}
			
			
	}
	
	public class PCB {
		int id;
		int pc;
		int frameIndex;
		String estado;
		int prioridade = 1;
		int[] tabelaPaginas;
		int tamTabelaPaginas;
		int tamPag;
		
	}
	
	// Gerente de Processos
	public class GerenteDeProcessos {
		
		public PCB pcb;
		
		int nroProcessos = 0;
		int tamProcesso;
		List<PCB> listaTodosProcessos = new ArrayList<PCB>();
		List<PCB> filaProcessosRodando = new ArrayList<PCB>();
		List<PCB> filaProcessosProntos = new ArrayList<PCB>();
		List<PCB> filaProcessosBloquados = new ArrayList<PCB>();
		
		public boolean criaProcesso(Word[] programa) {
			System.out.print("\n");
			System.out.print("\n");
			System.out.println("=============== CRIANDO UM NOVO PROCESSO ===============");
			System.out.println("||");
			System.out.println("|| -> Comunicando-se com o Gerente de Memoria.....");
			System.out.println("||");
			System.out.println("|| -> Enviando requisicao de alocacao.....");
			System.out.println("||");
			
			// cria PCB
			pcb = new PCB();
			
			// verifica tamanho
			tamProcesso = getTamProcesso(programa);
			
			// pede aloca��o de mem ao GM
				// se nao tem mem retorna false
			pcb.tabelaPaginas = vm.gerenteMemoria.aloca(tamProcesso);
			pcb.tamTabelaPaginas = vm.gerenteMemoria.tamTabelaPaginas;
			pcb.tamPag = vm.gerenteMemoria.tamPag;
			
			
			// carrega o programa nos frames alocados
			System.out.println("||");
			System.out.println("|| -> Carregando o programa nos frames.....");
			System.out.println("||");
			monitor.cargaFrames(programa);
			System.out.println("||");
			System.out.println("|| -> Gerando listas de processos.....");
			System.out.println("||");
			
			// seta demais parametros do PCB (id, pc=0, etc)
			pcb.id = nroProcessos++;
			pcb.pc = 0;
			pcb.frameIndex = 0;
			
			// coloca PCB na fila de prontos
			   listaTodosProcessos.add(pcb);
			   filaProcessosProntos.add(pcb);
			   pcb.estado = "pronto";
			
			   printaTodasListas();
			   
			   System.out.println("||");
			   System.out.println("|| -> Novo processo criado com sucesso!");
			   System.out.println("||");
			   System.out.println("============================================");
			   
			return true;
		}
		
		public void desalocaProcesso(int idProcesso) {
			PCB pcbPrograma = getPCBForID(idProcesso);
			vm.gerenteMemoria.desaloca(pcbPrograma.tabelaPaginas, pcbPrograma.tamTabelaPaginas);
			listaTodosProcessos.remove(pcbPrograma);
			filaProcessosRodando.remove(pcbPrograma);
			filaProcessosProntos.remove(pcbPrograma);
			filaProcessosBloquados.remove(pcbPrograma);
		}
		
	public int getTamProcesso (Word[] programa) {
		int tam = programa.length;
		return tam;
	}
	
	public int getNroProcessos() {
		return nroProcessos;
	}
	
	public PCB getPCBForID(int id) {
			PCB pcbDesejado = new PCB();
			boolean processoEstaNaLista = false;
		for(int i=0; i<listaTodosProcessos.size(); i++) {
			if (listaTodosProcessos.get(i).id == id) {
				pcbDesejado = listaTodosProcessos.get(i);
				processoEstaNaLista = true;
				return pcbDesejado;
			} else {
				// TRATAR ERRO
				processoEstaNaLista = false;
			}
		}
		if (processoEstaNaLista == false) {
			System.out.println("-------> Nenhum processo foi encontrado para o ID fornecido");
		}
		
		return pcbDesejado;
	}

	public void printaFramesDaTabelaPaginasDoProcesso(int idProcesso) {
		PCB pcbDoProcesso = getPCBForID(idProcesso);
		int[] tabelaPaginasDoProcesso = pcbDoProcesso.tabelaPaginas;
		int tamTabelaPaginasDoProcesso = pcbDoProcesso.tamTabelaPaginas;
		int tamPag = vm.gerenteMemoria.tamPag;
		
		System.out.println("||");
		for(int i=0; i<tamTabelaPaginasDoProcesso ; i++) {
			System.out.print("|| --------- FRAME[");
			System.out.print(tabelaPaginasDoProcesso[i]);
			System.out.println("] ---------");
			for(int j=0; j<tamPag; j++) {
				System.out.print("||   [");
				System.out.print(j);
				System.out.print("] ");
				System.out.print(vm.gerenteMemoria.frames[tabelaPaginasDoProcesso[i]].pagina[j].opc); 
				System.out.print("  ");
				System.out.print(vm.gerenteMemoria.frames[tabelaPaginasDoProcesso[i]].pagina[j].r1); 
				System.out.print(" ");
				System.out.print(vm.gerenteMemoria.frames[tabelaPaginasDoProcesso[i]].pagina[j].r2); 
				System.out.print(" ");
				System.out.println(vm.gerenteMemoria.frames[tabelaPaginasDoProcesso[i]].pagina[j].p);
			}
			System.out.println("||");
		}
	}
	
	public void printaTabelaPaginas(int idProcesso) {
		PCB pcbPograma = getPCBForID(idProcesso);
		int[] tabelaPaginasPCB =  pcbPograma.tabelaPaginas;
		int tamTabelaPaginasPCB = pcbPograma.tamTabelaPaginas;
			System.out.println("     --------------");
			System.out.println("     | PG | FRAME |");
			System.out.println("     --------------");
			for(int i=0; i<tamTabelaPaginasPCB ; i++) {	
				
				System.out.print("     | ");
				System.out.print(i);
				System.out.print(" |   ");
				System.out.print(tabelaPaginasPCB[i]);
				System.out.println("   |");
				System.out.println("     --------------");
			}
		
	}
	
	public void setParametrosIniciaisAoFinalizarCompletamentePrograma(int idProcesso) {
		PCB pcbPograma = getPCBForID(idProcesso);
		pcbPograma.pc = 0;
		pcbPograma.frameIndex = 0;
		filaProcessosRodando.remove(pcbPograma);
		filaProcessosProntos.add(pcbPograma);
		printaTodasListas();
	}
	
	public void printaTodasListas() {
		System.out.println("=============== LISTAS DE PROCESSOS POR ID ===============");
		System.out.print("|| -> Todos processos: ");
		for(int i=0; i<listaTodosProcessos.size(); i++) {
			System.out.print(listaTodosProcessos.get(i).id);
			System.out.print("  ");
		}
		System.out.print("\n");
		System.out.print("|| -> Rodando: ");
		for(int i=0; i<filaProcessosRodando.size(); i++) {
			System.out.print(filaProcessosRodando.get(i).id);
			System.out.print("  ");
		}
		System.out.print("\n");
		System.out.print("|| -> Prontos: ");
		for(int i=0; i<filaProcessosProntos.size(); i++) {
			System.out.print(filaProcessosProntos.get(i).id);
			System.out.print("  ");
		}
		System.out.print("\n");
		System.out.print("|| -> Bloquados: ");
		for(int i=0; i<filaProcessosBloquados.size(); i++) {
			System.out.print(filaProcessosBloquados.get(i).id);
			System.out.print("  ");
		}
		System.out.print("\n");
		System.out.println("========================================================");
		
	}
	
	public boolean programaEstaNaLista(List<PCB> lista, int idProcesso) {
		PCB pcbPograma = getPCBForID(idProcesso);
		if (lista.contains(pcbPograma)) {
			return true;
		} else {
			System.out.println("------------> Esta tentando remover da fila um elemento que nao esta nela");
			return false;
		}
	}

	
	public void dumpID(PCB pcb) {
		System.out.println("=============== DUMP PCB ===============");
		System.out.println("||");
		System.out.print("|| -> ID: ");
		System.out.println(pcb.id);
		
		System.out.print("|| -> PC: ");
		System.out.println(pcb.pc);
		
		System.out.print("|| -> Estado: ");
		System.out.println(pcb.estado);
		
		System.out.print("|| -> Prioridade: ");
		System.out.println("FALTA SETAR");
		
		System.out.println("|| -> Tabela de Paginas: ");
		
		System.out.println("||     --------------");
		System.out.println("||     | PG | FRAME |");
		System.out.println("||     --------------");
		for(int i=0; i<pcb.tamTabelaPaginas; i++) {	
		
			System.out.print("||     | ");
			System.out.print(i);
			System.out.print(" |   ");
			System.out.print(pcb.tabelaPaginas[i]);
			System.out.println("   |");
			System.out.println("||     --------------");
		}
		System.out.println("||");
		
		System.out.println("|| -> Conteudo dos frames de memoria desse processo: ");
		
		for(int i=0; i<pcb.tamTabelaPaginas ; i++) {
			System.out.print("||   ---------------- FRAME[");
			System.out.print(pcb.tabelaPaginas[i]);
			System.out.println("] ----------------");
			for(int j=0; j<pcb.tamPag; j++) {
				System.out.print("||   [");
				System.out.print(j);
				System.out.print("] ");
				System.out.print(vm.gerenteMemoria.frames[pcb.tabelaPaginas[i]].pagina[j].opc); 
				System.out.print("  ");
				System.out.print(vm.gerenteMemoria.frames[pcb.tabelaPaginas[i]].pagina[j].r1); 
				System.out.print(" ");
				System.out.print(vm.gerenteMemoria.frames[pcb.tabelaPaginas[i]].pagina[j].r2); 
				System.out.print(" ");
				System.out.println(vm.gerenteMemoria.frames[pcb.tabelaPaginas[i]].pagina[j].p);
			}
			System.out.println("||");
		}
		
		System.out.println("==========================================");
	}
	
		
	}
	
	
	// -------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// ------------------- S I S T E M A
	// --------------------------------------------------------------------

	public VM vm;
	public Monitor monitor;
	
	public static Programas progs;
	
	Word[] programa = null;
	String programaString = "";

	public Sistema() { // a VM com tratamento de interrupcoes
		vm = new VM();
		monitor = new Monitor();
		
		progs = new Programas();
			
	}

	public void roda() {
		while (true) {
		printInterfaceInterativa();
		}
		//vm.gerenteMemoria.aloca(programa.length); // Requisicao de alocacao
		/*
		System.out.println("---------------------------------- programa alocado ");
		//monitor.carga(programa, vm.m);
		//monitor.cargaFrames(programa);
		System.out.println("---------------------------------- programa carregado ");
		//monitor.dump(vm.m, 0, programa.length);
		//monitor.executa();
		System.out.println("---------------------------------- apos execucao ");
		//monitor.dump(vm.m, 0, programa.length);
		vm.gerenteMemoria.printaFramesDaTabelaPaginas();*/
	}

	public int leInteiro() {
		Scanner ler = new Scanner(System.in);
		System.out.print("\n");
		System.out.println(">>> System Call: Leitura de Teclado <<<");
		System.out.print("	>>> Insira um valor inteiro: ");
		int valorLido = ler.nextInt(); // Le um inteiro do teclado
		System.out.print("\n");

		return valorLido;
	}

	public void escreveIntTela(int valorOutput) {
		System.out.print("\n");
		System.out.println(">>> System Call: Escrita na Tela <<<");
		System.out.print("	>>> Valor inteiro: ");
		System.out.print(valorOutput);
		System.out.print("\n");
	}

	public void trataInterrupcao(Interrupt interrupt, int idPrograma) {
		switch (interrupt) {
			case STOP:
				System.out.println("||");
				System.out.println("||");
				System.out.println("||  >>> System Interruption: FINAL DO PROGRAMA <<<");
				System.out.println("||");
				System.out.println("||");
				
				System.out.println("||");
				System.out.println("||   ########## FINAL DA EXECUCAO ##########");
				System.out.println("||");
				
				vm.gerenteProcessos.setParametrosIniciaisAoFinalizarCompletamentePrograma(idPrograma);
				break;
			case ENDERECO_INVALIDO:
				System.out.print("\n");
				System.out.print("\n");
				System.out.println(">>> System Interruption: ENDERECO INVALIDO <<<");
				System.out.print("\n");
				System.out.print("\n");
				break;
			case INSTRUCAO_INVALIDA:
				System.out.print("\n");
				System.out.print("\n");
				System.out.println(">>> System Interruption: INSTRUCAO INVALIDA <<<");
				System.out.print("\n");
				System.out.print("\n");
				break;
			case OVERFLOW:
				System.out.print("\n");
				System.out.print("\n");
				System.out.println(">>> System Interruption: OVERFLOW <<<");
				System.out.print("\n");
				System.out.print("\n");
				break;
			default:
				// System.out.println("NO INTERRUPTION");
				break;
		}
	}
	
public void checaPC(int pc, PCB pcbPrograma) {
	//int tamPagina = vm.gerenteMemoria.tamPag;
	int tamPagina = pcbPrograma.tamPag;

	if(pc>=tamPagina) {
		//System.out.print("entrei");
		//vm.gerenteMemoria.pcForFrames = 0;
		pcbPrograma.pc = 0;
		vm.gerenteMemoria.indexForFrames++;
		pcbPrograma.frameIndex++;
		vm.cpu.setContext(pcbPrograma.id); 
	}
	//printDebugChecaPC(pc);	
}

public void printDebugChecaPC(int pc) {
	System.out.print("\n");
	System.out.println("---- DEBUG CHECK PC ----");
	System.out.print("-> PC interno da funcao: ");
	System.out.println(pc);	
	System.out.print("-> PC real da cpu: ");
	System.out.println(vm.cpu.pc);
	System.out.println("------------------------");
	System.out.print("\n");
}

public void mudaPC(int frame, int offset, PCB pcbPrograma) {
	//vm.gerenteMemoria.pcForFrames = offset;
	pcbPrograma.pc = offset;
	vm.gerenteMemoria.indexForFrames = frame;
	pcbPrograma.frameIndex = frame;
	vm.cpu.setContext(pcbPrograma.id);
}
	
public void chamaSistema() {
	
	CPU cpuAccess = vm.cpu;
	PCB pcbDesejado = vm.gerenteProcessos.getPCBForID(1);
	
	vm.gerenteMemoria.traduzEndereco(cpuAccess.reg[9], "AM", pcbDesejado);
	int frameTraduzido = vm.gerenteMemoria.getFrameTraduzido();
	int pagTraduzida = vm.gerenteMemoria.getPagTraduzida();
	
	if (cpuAccess.reg[8] == 1) { // IN
		int valorLido = leInteiro(); // Chama o metodo que le um inteiro do teclado
		if (valorLido >= -10000 && valorLido <= 10000) { // Verifica se esta dentro do range valido
			
			vm.gerenteMemoria.frames[frameTraduzido].pagina[pagTraduzida].p = valorLido;
			//cpuAccess.m[cpuAccess.reg[9]].p = valorLido; // Coloca o valor lido no endereco de mem. armazenado no reg. 9
			cpuAccess.pc++;
		} else {
			interrupt = Interrupt.OVERFLOW; // Se estiver fora do range interrompe por overflow
		}

	} else if (cpuAccess.reg[8] == 2) { // OUT
		int valorOut = vm.gerenteMemoria.frames[frameTraduzido].pagina[pagTraduzida].p;
		//int valorOut = cpuAccess.m[cpuAccess.reg[9]].p; // O endereco de mem. cujo valor deve-se escrever na tela esta
									// armazenado no reg. 9
		escreveIntTela(valorOut); // Chama o metodo que escreve um inteiro na tela
		cpuAccess.pc++;
	} else {
		// Caso hajam novas chamadas de sistema
	}
		
}

public void printInterfaceInterativa() {
	Scanner ler = new Scanner(System.in);
	System.out.println("\n");
	System.out.println("\n");
	System.out.println("\n");
	System.out.println("===================== INTERFACE DE PROCESSOS =====================");
	System.out.println("||");
	System.out.println("|| -> O que vc deseja fazer?");
	System.out.println("||");
	System.out.println("||       (1) Criar um novo processo;");
	System.out.println("||       (2) Executar um processo existente;");
	System.out.println("||       (3) Listar o conteudo do PCB e o conteudo de cada frame de memoria de um processo especifico;");
	System.out.println("||       (4) Listar todos os frames de memoria do sistema;");
	System.out.println("||       (5) Excluir um processo especifico;");
	System.out.println("||       (6) Printar todas as listas de processos;");
	System.out.println("||");
	System.out.print("||       -> ");
	int valorLido = ler.nextInt();
	System.out.println("||");
	System.out.println("==================================================================");
	
	processaComandoLido(valorLido);
}

public void processaComandoLido(int comando) {
	switch (comando) {
	case 1:
		printaInterfaceEscolhaNovoPrograma();
		this.programa = escolhePrograma(programaString);
		vm.gerenteProcessos.criaProcesso(this.programa);
		break;
		
	case 2:
		int idPrograma = getProgamaUsuarioGostariaDeExecutar();
		monitor.executa(idPrograma);
		
		System.out.println("||");
		System.out.println("||   ########## FRAMES DO PROGRAMA APOS EXECUCAO ##########");
		System.out.println("||");
		
		vm.gerenteProcessos.printaFramesDaTabelaPaginasDoProcesso(idPrograma);
		
		System.out.println("======================================");
		
		break;
		
	case 3:
		int idInserido = printaInterfaceInsercaoID();
		for(int i=0; i<vm.gerenteProcessos.listaTodosProcessos.size(); i++) {
			if (vm.gerenteProcessos.listaTodosProcessos.get(i).id == idInserido) {
				vm.gerenteProcessos.dumpID(vm.gerenteProcessos.listaTodosProcessos.get(i));
			}
		}
		break;
		
	case 4:
		vm.gerenteMemoria.printaFrames();
		break;
		
	case 5:
		int idDesaloca = printaInterfaceInsercaoID();
		PCB pcbPrograma = vm.gerenteProcessos.getPCBForID(idDesaloca);
		vm.gerenteMemoria.desaloca(pcbPrograma.tabelaPaginas, pcbPrograma.tamTabelaPaginas);
		break;
		
	case 6:
		vm.gerenteProcessos.printaTodasListas();
		break;
	default:
		System.out.println("|| -> ATENCAO: Selecione uma das opcoes da lista");
		break;
	}
}

public void printaInterfaceEscolhaNovoPrograma() {
	Scanner ler = new Scanner(System.in);
	System.out.print("|| -> Para qual programa vc gostaria de criar um processo? ");
	String nomePrograma = ler.nextLine();
	System.out.println("==================================================================");
	this.programaString = nomePrograma;
}

public int getProgamaUsuarioGostariaDeExecutar() {
	Scanner ler = new Scanner(System.in);
	System.out.print("|| -> Informe a id do processo que deseja executar: ");
	int idPrograma = ler.nextInt();
	return idPrograma;
}

public Word[] escolhePrograma(String nomePrograma) {
	Word[] programaEscolhido = progs.progMinimo;
	
	switch(nomePrograma) {
	case "fibonacci10":
		programaEscolhido = progs.fibonacci10;
		break;
	case "progMinimo":
		programaEscolhido = progs.progMinimo;
		break;
	case "fatorial":
		programaEscolhido = progs.fatorial;
		break;
	case "NewInstructionTester":
		programaEscolhido = progs.NewInstructionTester;
		break;
	case "PA":
		programaEscolhido = progs.PA;
		break;
	case "PB":
		programaEscolhido = progs.PB;
		break;
	case "PC":
		programaEscolhido = progs.PC;
		break;
	case "InterruptionTester":
		programaEscolhido = progs.InterruptionTester;
		break;
	case "SystemCallTester":
		programaEscolhido = progs.SystemCallTester;
		break;
	default:
		System.out.println("################ O PROGRAMA ESCOLHIDO NAO CONSTA NO SISTEMA ################");
		System.exit(0);
	}
	return programaEscolhido;
}

public int printaInterfaceInsercaoID() {
	Scanner ler = new Scanner(System.in);
	System.out.print("|| -> Insira o ID do processo: ");
	int id = ler.nextInt();
	System.out.println("==================================================================");
	return id;
}

	// ------------------- S I S T E M A - fim
	// --------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// ------------------- instancia e testa sistema
	public static void main(String args[]) {
		Sistema s = new Sistema();
		
		s.roda();
		
		// s.roda(progs.fibonacci10); // "progs" significa acesso/referencia ao programa
		// em memoria secundaria
		//s.roda(progs.progMinimo);
		//s.roda(progs.fatorial);
		// s.roda(progs.NewInstructionTester);
		//s.roda(progs.PA);
		//s.roda(progs.PB);
		//s.roda(progs.PC);
		//s.roda(progs.InterruptionTester);
		//s.roda(progs.SystemCallTester);
		
		//s.gerenteMemoria.aloca(150);
		//s.gerenteMemoria.aloca(30);
		//s.gerenteMemoria.aloca(10);
		
		

	}
	// -------------------------------------------------------------------------------------------------------
	// --------------- TUDO ABAIXO DE MAIN Ãƒâ€° AUXILIAR PARA FUNCIONAMENTO DO
	// SISTEMA - nao faz parte

	// -------------------------------------------- programas aa disposicao para
	// copiar na memoria (vide carga)
	public class Programas {
		public Word[] progMinimo = new Word[] {
				// OPCODE R1 R2 P :: VEJA AS COLUNAS VERMELHAS DA TABELA DE DEFINICAO DE
				// OPERACOES
				// :: -1 SIGNIFICA QUE O PARAMETRO NAO EXISTE PARA A OPERACAO DEFINIDA
				new Word(Opcode.LDI, 0, -1, 999), new Word(Opcode.STD, 0, -1, 10), new Word(Opcode.STD, 0, -1, 11),
				new Word(Opcode.STD, 0, -1, 12), new Word(Opcode.STD, 0, -1, 13), new Word(Opcode.STD, 0, -1, 14),
				new Word(Opcode.STOP, -1, -1, -1) };

		public Word[] fibonacci10 = new Word[] { // mesmo que prog exemplo, so que usa r0 no lugar de r8
				new Word(Opcode.LDI, 1, -1, 0), new Word(Opcode.STD, 1, -1, 20), // 20 posicao de memoria onde inicia a
																					// serie de fibonacci gerada
				new Word(Opcode.LDI, 2, -1, 1), new Word(Opcode.STD, 2, -1, 21), new Word(Opcode.LDI, 0, -1, 22),
				new Word(Opcode.LDI, 6, -1, 6), new Word(Opcode.LDI, 7, -1, 30), new Word(Opcode.LDI, 3, -1, 0),
				new Word(Opcode.ADD, 3, 1, -1), new Word(Opcode.LDI, 1, -1, 0), new Word(Opcode.ADD, 1, 2, -1),
				new Word(Opcode.ADD, 2, 3, -1), new Word(Opcode.STX, 0, 2, -1), new Word(Opcode.ADDI, 0, -1, 1),
				new Word(Opcode.SUB, 7, 0, -1), new Word(Opcode.JMPIG, 6, 7, -1), new Word(Opcode.STOP, -1, -1, -1), // POS
																														// 16
				new Word(Opcode.DATA, -1, -1, -1), new Word(Opcode.DATA, -1, -1, -1), new Word(Opcode.DATA, -1, -1, -1),
				new Word(Opcode.DATA, -1, -1, -1), // POS 20
				new Word(Opcode.DATA, -1, -1, -1), new Word(Opcode.DATA, -1, -1, -1), new Word(Opcode.DATA, -1, -1, -1),
				new Word(Opcode.DATA, -1, -1, -1), new Word(Opcode.DATA, -1, -1, -1), new Word(Opcode.DATA, -1, -1, -1),
				new Word(Opcode.DATA, -1, -1, -1), new Word(Opcode.DATA, -1, -1, -1), new Word(Opcode.DATA, -1, -1, -1) // ate
																														// aqui
																														// -
																														// serie
																														// de
																														// fibonacci
																														// ficara
																														// armazenada
		};

		public Word[] fatorial = new Word[] { // este fatorial so aceita valores positivos. nao pode ser zero
												// linha coment
				new Word(Opcode.LDI, 0, -1, 6), // 0 r0 ÃƒÂ© valor a calcular fatorial
				new Word(Opcode.LDI, 1, -1, 1), // 1 r1 ÃƒÂ© 1 para multiplicar (por r0)
				new Word(Opcode.LDI, 6, -1, 1), // 2 r6 ÃƒÂ© 1 para ser o decremento
				new Word(Opcode.LDI, 7, -1, 8), // 3 r7 tem posicao de stop do programa = 8
				new Word(Opcode.JMPIE, 7, 0, 0), // 4 se r0=0 pula para r7(=8)
				new Word(Opcode.MULT, 1, 0, -1), // 5 r1 = r1 * r0
				new Word(Opcode.SUB, 0, 6, -1), // 6 decrementa r0 1
				new Word(Opcode.JMP, -1, -1, 4), // 7 vai p posicao 4
				new Word(Opcode.STD, 1, -1, 10), // 8 coloca valor de r1 na posicao 10
				new Word(Opcode.STOP, -1, -1, -1), // 9 stop
				new Word(Opcode.DATA, -1, -1, -1) }; // 10 ao final o valor do fatorial estara na posicao 10 da
														// memoria

		public Word[] PA = new Word[] { // Reutiliza o codigo do programa "fibonacci10", que eh apenas incrementado
				new Word(Opcode.LDD, 4, -1, 31), // 0 Carrega no registrador R4 o valor da pos. 31 de mem. (valor base
													// para logica do programa)
				new Word(Opcode.JMPIGK, -1, 4, 5), // 1 Se maior que 0 pula para linha 5 do codigo
				new Word(Opcode.LDI, 1, -1, -1), // 2 Se nao, coloca -1 na primeira pos. de mem. de saida(pos. 33)
				new Word(Opcode.STD, 1, -1, 33), // 3
				new Word(Opcode.STOP, -1, -1, -1), // 4 Finaliza a execucao do programa

				new Word(Opcode.LDI, 1, -1, 0), // 5 Gera o primeiro valor da sequencia de fibonacci (0)
				new Word(Opcode.STD, 1, -1, 33), // 6 Salva na primeira pos. de mem. destinada a sequencia (pos. 33)
				new Word(Opcode.SUBI, 4, -1, 1), // 7 Faz R4-1, pois R4 armazenava o numero total de valores desejados,
													// utilizaremos R4 como index
				new Word(Opcode.JMPIGK, -1, 4, 10), // 8 Finaliza se for igual a 0, se nao, segue a execucao
				new Word(Opcode.STOP, -1, -1, -1), // 9

				new Word(Opcode.LDI, 2, -1, 1), // 10 Gera o segundo valor da sequencia de fibonacci (1)
				new Word(Opcode.STD, 2, -1, 34), // 11 Salva na segunda pos. de mem. destinada a sequencia (pos. 34)
				new Word(Opcode.SUBI, 4, -1, 1), // 12 Faz R4-1
				new Word(Opcode.JMPIGK, -1, 4, 15), // 13 Finaliza se for igual a 0, se nao, segue a execucao
				new Word(Opcode.STOP, -1, -1, -1), // 14

				new Word(Opcode.LDI, 0, -1, 35), // 15 A partir daqui, calcularemos matematicamente os proximos valores
													// da sequencia da pos. 35 da mem. em diante
				new Word(Opcode.LDI, 7, -1, 1), // 16
				new Word(Opcode.LDI, 6, -1, 19), // 17 19 eh a pos. de mem. do inicio do loop que faz o calculo de novos
													// valores
				new Word(Opcode.MOVE, 7, 4, -1), // 18 Faz R7<-[R4] (R4 tem o num. de valores da sequencia que faltam
													// ser calculados)
				new Word(Opcode.JMPIEK, -1, 7, 29), // 19 Sai do loop quando R7 zera (R7 tem o num. de valores da
													// sequencia que faltam ser calculados)
				new Word(Opcode.LDI, 3, -1, 0), // 20
				new Word(Opcode.ADD, 3, 1, -1), // 21
				new Word(Opcode.LDI, 1, -1, 0), // 22
				new Word(Opcode.ADD, 1, 2, -1), // 23
				new Word(Opcode.ADD, 2, 3, -1), // 24
				new Word(Opcode.STX, 0, 2, -1), // 25
				new Word(Opcode.ADDI, 0, -1, 1), // 26
				new Word(Opcode.SUBI, 7, -1, 1), // 27
				new Word(Opcode.JMPIG, 6, 7, -1), // 28 Volta para o inicio do loop
				new Word(Opcode.STOP, -1, -1, -1), // 29 Termina a execucao
				// Area de dados
				new Word(Opcode.DATA, -1, -1, -1), // 30
				new Word(Opcode.DATA, -1, -1, 8), // 31 <----- Se o valor armazenado nesta pos. de mem. for menor que 0,
													// coloca -1 no inicio da pos. de mem. para saida (pos 25). Se for
													// maior, este eh o num. de valores da sequencia
				new Word(Opcode.DATA, -1, -1, -1), // 32
				new Word(Opcode.DATA, -1, -1, -1), // 33 Sequencia salva desta posicao.....
				new Word(Opcode.DATA, -1, -1, -1), // 34
				new Word(Opcode.DATA, -1, -1, -1), // 35
				new Word(Opcode.DATA, -1, -1, -1), // 36
				new Word(Opcode.DATA, -1, -1, -1), // 37
				new Word(Opcode.DATA, -1, -1, -1), // 38
				new Word(Opcode.DATA, -1, -1, -1), // 39
				new Word(Opcode.DATA, -1, -1, -1), // 40
				new Word(Opcode.DATA, -1, -1, -1), // 41
				new Word(Opcode.DATA, -1, -1, -1) // 42 ...ate esta
		};

		public Word[] PB = new Word[] { // Reutiliza o codigo do programa "fatorial", que eh apenas incrementado
				new Word(Opcode.LDD, 0, -1, 14), // 0 Carrega no registrador 0 o valor da pos. 10 de mem. (valor base
													// para logica do programa)

				new Word(Opcode.JMPILK, -1, 0, 11), // 1 Se R0<0, pula para a linha 11 do codigo

				new Word(Opcode.LDI, 1, -1, 1), // 2 R1 recebe 1 para multiplicar (por R0)
				new Word(Opcode.LDI, 6, -1, 1), // 3 R6 recebe 1 para ser o decremento
				new Word(Opcode.LDI, 7, -1, 9), // 4 R7 tem posicao do save anterior ao stop do programa = 9
				new Word(Opcode.JMPIE, 7, 0, 0), // 5 Se R0=0 pula para R7(=9)
				new Word(Opcode.MULT, 1, 0, -1), // 6 R1 = R1 * R0
				new Word(Opcode.SUB, 0, 6, -1), // 7 Decrementa R0 (R0-1)
				new Word(Opcode.JMP, -1, -1, 4), // 8 Vai para a posicao 4
				new Word(Opcode.STD, 1, -1, 15), // 9 Coloca valor de R1 na posicao 15
				new Word(Opcode.STOP, -1, -1, -1), // 10 Finaliza a execucao do programa

				new Word(Opcode.LDI, 1, -1, -1), // 11
				new Word(Opcode.STD, 1, -1, 15), // 12 Salva o valor -1 na pos. de mem. destinada ao output da resposta
													// (pos. 15)
				new Word(Opcode.STOP, -1, -1, -1), // 13 Finaliza a execucao do programa
				// Ã�rea de dados
				new Word(Opcode.DATA, -1, -1, 7), // 14 <---- Valor a se calcular o fatorial
				new Word(Opcode.DATA, -1, -1, -1) // 15 Valor do fatorial calculado estara nesa pos. de mem.
		};

		public Word[] PC = new Word[] { // "Bubble Sort"
				new Word(Opcode.LDI, 0, -1, 10), // 0 Carregando valores do array na memoria
				new Word(Opcode.STD, 0, -1, 34), // 1

				new Word(Opcode.LDI, 0, -1, 15), // 2
				new Word(Opcode.STD, 0, -1, 35), // 3

				new Word(Opcode.LDI, 0, -1, 7), // 4
				new Word(Opcode.STD, 0, -1, 36), // 5

				new Word(Opcode.LDI, 0, -1, -3), // 6
				new Word(Opcode.STD, 0, -1, 37), // 7

				new Word(Opcode.LDI, 0, -1, 0), // 8
				new Word(Opcode.STD, 0, -1, 38), // 9

				new Word(Opcode.LDI, 0, -1, 3), // 10
				new Word(Opcode.STD, 0, -1, 39), // 11

				new Word(Opcode.LDI, 0, -1, -5), // 12
				new Word(Opcode.STD, 0, -1, 40), // 13 Fim do array

				new Word(Opcode.LDI, 7, -1, 40), // 14 R7 recebe o valor da pos. max de mem. do nosso vetor (pos. 40)
				new Word(Opcode.LDI, 0, -1, 34), // 15 Carrega a primeira pos. para dentro do R0 (pos. 34)
				new Word(Opcode.LDI, 1, -1, 35), // 16 Carrega a segunda pos. para dentro do R1 (pos. 35)
				new Word(Opcode.LDI, 5, -1, 0), // 17 R5 sera o contador de numeros que estao em ordem crescente
				new Word(Opcode.LDI, 6, -1, 5), // 18 O valor max. de num. em ordem é 6(como temos 7 numeros e a
												// comparacao eh feita de 2 em 2, esse valor max.=6), colocamos o valor
												// 5 no R6 pois na linha 19 faremos uma comparacao JMPIGT
				// inicio loop
				new Word(Opcode.JMPIGT, 5, 6, 33), // 19 Quando o contador de numeros em ordem chegar ao seu valor max.
													// termina a execucao (quando o contador chegar a 6, pois 6>5 then
													// termina execucao)
				new Word(Opcode.JMPIGT, 1, 7, 15), // 20 Quando alcancar a pos. max. de mem. (pos. 46 que esta
													// registrada no R7), volta para linha 15 para voltar para a
													// primeira pos. e zerar o contador de numeros em ordem
				new Word(Opcode.LDX, 2, 0, -1), // 21 Carrega em R2 o valor de R0
				new Word(Opcode.LDX, 3, 1, -1), // 22 Carrega em R3 o valor de R1
				new Word(Opcode.JMPIGT, 2, 3, 28), // 23 Compara se o primeiro valor eh maior que o segundo (da esq.
													// para a direita na mem.)
				new Word(Opcode.ADDI, 0, -1, 1), // 24
				new Word(Opcode.ADDI, 1, -1, 1), // 25 Se os valores comparados ja estiverem em ordem crescente na
													// memoria, incrementa a pos. dos apontadores e reinicia o loop
				new Word(Opcode.ADDI, 5, -1, 1), // 26 Incrementa o contador de numeros crescentes
				new Word(Opcode.JMP, -1, -1, 19), // 27
				new Word(Opcode.STX, 0, 3, -1), // 28 Se o primeiro valor for maior que o segundo, suas posicoes sao
												// invertidas na memoria
				new Word(Opcode.STX, 1, 2, -1), // 29
				new Word(Opcode.ADDI, 0, -1, 1), // 30 Incrementa os registradores que estao apontando para as pos. de
													// mem. (R0 e R1)
				new Word(Opcode.ADDI, 1, -1, 1), // 31
				new Word(Opcode.JMP, -1, -1, 19), // 32 Volta para o inicio do loop

				new Word(Opcode.STOP, -1, -1, -1), // 33
				// Area de dados
				new Word(Opcode.DATA, -1, -1, -1), // 34 Vetor vai daqui....
				new Word(Opcode.DATA, -1, -1, -1), // 35
				new Word(Opcode.DATA, -1, -1, -1), // 36
				new Word(Opcode.DATA, -1, -1, -1), // 37
				new Word(Opcode.DATA, -1, -1, -1), // 38
				new Word(Opcode.DATA, -1, -1, -1), // 39
				new Word(Opcode.DATA, -1, -1, -1), // 40 ...ate aqui
		};

		public Word[] NewInstructionTester = new Word[] { // Testa as novas instrucoes implementadas
				new Word(Opcode.JMP, -1, -1, 14), // 0 // Inicia o prog. na pos. 14 da mem.
				// Area de dados
				new Word(Opcode.DATA, -1, -1, 200), // 1 - Ocupada
				new Word(Opcode.DATA, -1, -1, -1), // 2 - Ocupada
				new Word(Opcode.DATA, -1, -1, -1), // 3 - Ocupada
				new Word(Opcode.DATA, -1, -1, -1), // 4 - Ocupada
				new Word(Opcode.DATA, -1, -1, -1), // 5 - Ocupada
				new Word(Opcode.DATA, -1, -1, -1), // 6 - Ocupada
				new Word(Opcode.DATA, -1, -1, 38), // 7 - Ocupada
				new Word(Opcode.DATA, -1, -1, -1), // 8 - Livre
				new Word(Opcode.DATA, -1, -1, -1), // 9 - Livre
				new Word(Opcode.DATA, -1, -1, -1), // 10 - Livre
				new Word(Opcode.DATA, -1, -1, -1), // 11 - Livre
				new Word(Opcode.DATA, -1, -1, -1), // 12 - Livre
				new Word(Opcode.DATA, -1, -1, -1), // 13 - Livre
				// Area de programa
				new Word(Opcode.LDD, 4, -1, 1), // 14 LDD - FUNCIONANDO (se insere em R4 o valor da pos. 1 da mem.)

				new Word(Opcode.JMPIGK, -1, 4, 17), // 15 JMPIGK - FUNCIONANDO (se salva corretamente o valor de R4 na
													// pos. 2 da mem.)
				new Word(Opcode.LDI, 4, -1, 100), // 16
				new Word(Opcode.STD, 4, -1, 2), // 17

				new Word(Opcode.MOVE, 1, 4, -1), // 18 MOVE - FUNCIONANDO (se insere em R1 o valor de R4)

				new Word(Opcode.SUBI, 4, -1, 10), // 19 SUBI - FUNCIONANDO (se R4 passa a valer R4-10)

				new Word(Opcode.LDI, 3, -1, 0), // 20
				new Word(Opcode.JMPIEK, -1, 3, 23), // 21 JMPIEK - FUNCIONANDO (funcionando se salva o valor 0 na pos. 3
													// da mem.)
				new Word(Opcode.LDI, 3, -1, 100), // 22
				new Word(Opcode.STD, 3, -1, 2), // 23

				new Word(Opcode.LDI, 3, -1, 0), // 24
				new Word(Opcode.JMPILK, -1, 3, 27), // 25 JMPILK - FUNCIONANDO (funcionando se salva o valor 100 na pos.
													// 4 da mem.)
				new Word(Opcode.LDI, 3, -1, 100), // 26
				new Word(Opcode.STD, 3, -1, 4), // 27

				new Word(Opcode.LDI, 3, -1, 8), // 28
				new Word(Opcode.LDI, 4, -1, 5), // 29
				new Word(Opcode.JMPIGT, 3, 4, 32), // 30 JMPIGT - FUNCIONANDO (funcionando se salva o valor 8 na pos. 5
													// da mem.)
				new Word(Opcode.LDI, 3, -1, 100), // 31
				new Word(Opcode.STD, 3, -1, 5), // 32

				new Word(Opcode.LDI, 4, -1, 1), // 33
				new Word(Opcode.LDX, 5, 4, -1), // 34 LDX - FUNCIONANDO (funcionando se salva o valor 200 na pos. 6 da
												// mem.)
				new Word(Opcode.STD, 5, -1, 6), // 35

				new Word(Opcode.JMPIGM, -1, 4, 7), // 36 JMPIGM - FUNCIONANDO (funcionando se salva 45 na pos. 8 da
													// mem.)
				new Word(Opcode.LDI, 4, -1, 50), // 37
				new Word(Opcode.LDI, 4, -1, 45), // 38
				new Word(Opcode.STD, 4, -1, 8), // 39

				new Word(Opcode.STOP, -1, -1, -1) //
		};

		public Word[] InterruptionTester = new Word[] { new Word(Opcode.JMP, -1, -1, 14), // 0 // Inicia o prog. na pos.
																							// 14 da mem.
				// Area de dados
				new Word(Opcode.DATA, -1, -1, 1025), // 1
				new Word(Opcode.DATA, -1, -1, -1025), // 2
				new Word(Opcode.DATA, -1, -1, -1), // 3
				new Word(Opcode.DATA, -1, -1, -1), // 4
				new Word(Opcode.DATA, -1, -1, -1), // 5
				new Word(Opcode.DATA, -1, -1, -1), // 6
				new Word(Opcode.DATA, -1, -1, -1), // 7
				new Word(Opcode.DATA, -1, -1, -1), // 8
				new Word(Opcode.DATA, -1, -1, -1), // 9
				new Word(Opcode.DATA, -1, -1, -1), // 10
				new Word(Opcode.DATA, -1, -1, -1), // 11
				new Word(Opcode.DATA, -1, -1, -1), // 12
				new Word(Opcode.DATA, -1, -1, -1), // 13

				// Area de programa
				new Word(Opcode.LDI, 0, -1, 1025), // 14
				new Word(Opcode.LDI, 1, -1, 10000), // 15
				new Word(Opcode.LDI, 2, -1, -1025), // 16
				new Word(Opcode.LDI, 3, -1, 0), // 17
				new Word(Opcode.LDI, 4, -1, 0), // 18
				new Word(Opcode.LDI, 5, -1, 0), // 19
				new Word(Opcode.LDI, 6, -1, 0), // 20
				new Word(Opcode.LDI, 7, -1, 0), // 21

				// new Word(Opcode.XXX, 3, -1, 5), // 22 // IMPOSSIVEL DE TESTAR

				// Instrucoes de Memoria
				//new Word(Opcode.LDD, 1, -1, 1025), // 23 // LDD - FUNCIONANDO
				// --------------------------------
				//new Word(Opcode.LDX, 1, 0, -1), // 24 // LDX - FUNCIONANDO
				// --------------------------------
				// new Word(Opcode.STD, 1, 1025, -1), //25 // STD - FUNCIONANDO
				// --------------------------------
				// new Word(Opcode.STX, 0, 1, -1), // 26 // STX - FUNCIONANDO

				// Instrucoes Aritmeticas
				// new Word(Opcode.ADDI, 1, -1, 1), // 27 // ADDI - FUNCIONANDO PARA OVERFLOW(+)
				// E OVERFLOW(-)
				// new Word(Opcode.LDI, 1, -1, -10000), // 28
				// new Word(Opcode.ADDI, 1, -1, -1), // 29
				// --------------------------------
				// new Word(Opcode.SUBI, 1, -1, -1), // 30 // SUBI - FUNCIONANDO PARA
				// OVERFLOW(+) E OVERFLOW(-)
				// new Word(Opcode.LDI, 1, -1, -10000), // 31
				// new Word(Opcode.SUBI, 1, -1, 1), // 32
				// --------------------------------
				// new Word(Opcode.ADD, 0, 1, -1), // 33 // ADD - FUNCIONANDO PARA OVERFLOW(+) E
				// OVERFLOW(-)
				// new Word(Opcode.LDI, 1, -1, -10000), // 34
				// new Word(Opcode.ADD, 2, 1, -1), // 35
				// --------------------------------
				// new Word(Opcode.LDI, 1, -1, 15000), // 36
				// new Word(Opcode.SUB, 1, 0, -1), // 37 // SUB - FUNCIONANDO PARA OVERFLOW(+) E
				// OVERFLOW(-)
				// new Word(Opcode.LDI, 1, -1, -10000), // 38
				// new Word(Opcode.SUB, 1, 0, -1), // 39
				// --------------------------------
				// new Word(Opcode.MULT, 0, 1, -1), // 40 // MULT - FUNCIONANDO PARA OVERFLOW(+)
				// E OVERFLOW(-)
				// new Word(Opcode.MULT, 1, 2, -1), // 41

				// Instrucoes Jump
				// new Word(Opcode.JMP, -1, -1, 1024), // 42 // JMP - FUNCIONANDO PARA
				// ENDERECO(+) E ENDERECO(-)
				// new Word(Opcode.JMP, -1, -1, -2), // 43
				// --------------------------------
				// new Word(Opcode.JMPIGK, -1, 0, 1024), // 44 // JMPIGK - FUNCIONANDO PARA
				// ENDERECO(+) E ENDERECO(-)
				// new Word(Opcode.JMPIGK, -1, 0, -1), // 45
				// --------------------------------
				// new Word(Opcode.JMPI, 0, -1, -1), // 46 // JMPI - FUNCIONANDO PARA
				// ENDERECO(+) E ENDERECO(-)
				// new Word(Opcode.JMPI, 2, -1, -1), // 47
				// --------------------------------
				// new Word(Opcode.JMPIG, 0, 0, -1), // 48 // JMPIG - FUNCIONANDO PARA
				// ENDERECO(+) E ENDERECO(-)
				// new Word(Opcode.JMPIG, 2, 0, -1), // 49
				// --------------------------------
				// new Word(Opcode.JMPIL, 0, 2, -1), // 50 // JMPIL - FUNCIONANDO PARA
				// ENDERECO(+) E ENDERECO(-)
				// new Word(Opcode.JMPIL, 2, 2, -1), // 51
				// --------------------------------
				// new Word(Opcode.JMPIE, 0, 7, -1), // 52 // JMPIE - FUNCIONANDO PARA
				// ENDERECO(+) E ENDERECO(-)
				// new Word(Opcode.JMPIE, 2, 7, -1), // 53
				// --------------------------------
				// new Word(Opcode.JMPIM, -1, -1, 1), // 54 // JMPIM - TESTAR
				// --------------------------------
				// new Word(Opcode.JMPIGM, -1, 0, 1), // 55 // JMPIGM - FUNCIONANDO PARA
				// ENDERECO(+) E ENDERECO(-)
				// new Word(Opcode.JMPIGM, -1, 0, 2), // 56
				// --------------------------------
				// new Word(Opcode.JMPILM, -1, 2, 1), // 57 // JMPILM - FUNCIONANDO PARA
				// ENDERECO(+) E ENDERECO(-)
				// new Word(Opcode.JMPILM, -1, 2, 2), // 58
				// --------------------------------
				// new Word(Opcode.JMPIEM, -1, 7, 1), // 59 // JMPIEM - FUNCIONANDO PARA
				// ENDERECO(+) E ENDERECO(-)
				// new Word(Opcode.JMPIEM, -1, 7, 2), // 60
				// --------------------------------
				// new Word(Opcode.JMPIGT, 1, 0, 1025), // 61 // JMPIGT - FUNCIONANDO PARA
				// ENDERECO(+) E ENDERECO(-)
				// new Word(Opcode.JMPIGT, 1, 0, -1), // 62
				// --------------------------------

				new Word(Opcode.STOP, -1, -1, -1) // 63
		};

		public Word[] SystemCallTester = new Word[] { 
				new Word(Opcode.JMP, -1, -1, 9), // 0 // Inicia o prog. na pos. 9
																						// da mem.
				// Area de dados
				new Word(Opcode.DATA, -1, -1, -1), // 1 // Armazena input 1
				new Word(Opcode.DATA, -1, -1, -1), // 2 // Armazena input 2
				new Word(Opcode.DATA, -1, -1, -1), // 3 // Armazena input 3
				new Word(Opcode.DATA, -1, -1, 10), // 4 // Output 1
				new Word(Opcode.DATA, -1, -1, -5), // 5 // Output 2
				new Word(Opcode.DATA, -1, -1, 310), // 6 // Output 3
				new Word(Opcode.DATA, -1, -1, -1), // 7
				new Word(Opcode.DATA, -1, -1, -1), // 8

				// Area de programa
				new Word(Opcode.LDI, 8, -1, 1), // 9 // Valor para chamada de IN
				new Word(Opcode.LDI, 9, -1, 1), // 10 // Salva na pos. 1 de mem.
				new Word(Opcode.TRAP, -1, -1, -1), // 11 // Chama o sistema

				new Word(Opcode.LDI, 8, -1, 1), // 12 // Valor para chamada de IN
				new Word(Opcode.LDI, 9, -1, 2), // 13 // Salva na pos. 2 de mem.
				new Word(Opcode.TRAP, -1, -1, -1), // 14 // Chama o sistema

				new Word(Opcode.LDI, 8, -1, 1), // 15 // Valor para chamada de IN
				new Word(Opcode.LDI, 9, -1, 3), // 16 // Salva na pos. 3 de mem.
				new Word(Opcode.TRAP, -1, -1, -1), // 17 // Chama o sistema

				new Word(Opcode.LDI, 8, -1, 2), // 18 // Valor para chamada de OUT
				new Word(Opcode.LDI, 9, -1, 4), // 19 // Escreve na tela o inteiro salvo na pos. 4 da mem.
				new Word(Opcode.TRAP, -1, -1, -1), // 20 // Chama o sistema

				new Word(Opcode.LDI, 8, -1, 2), // 21 // Valor para chamada de OUT
				new Word(Opcode.LDI, 9, -1, 5), // 22 // Escreve na tela o inteiro salvo na pos. 5 da mem.
				new Word(Opcode.TRAP, -1, -1, -1), // 23 // Chama o sistema

				new Word(Opcode.LDI, 8, -1, 2), // 24 // Valor para chamada de OUT
				new Word(Opcode.LDI, 9, -1, 6), // 25 // Escreve na tela o inteiro salvo na pos. 6 da mem.
				new Word(Opcode.TRAP, -1, -1, -1), // 26 // Chama o sistema

				new Word(Opcode.STOP, -1, -1, -1) // 27
		};

	}

}
