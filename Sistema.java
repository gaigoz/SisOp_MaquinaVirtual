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

		public void setContext(int _pc) { // no futuro esta funcao vai ter que ser
			pc = _pc; // limite e pc (deve ser zero nesta versao)
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

		public void run() { // execucao da CPU supoe que o contexto da CPU, vide acima, esta devidamente
							// setado

			interrupt = Interrupt.NULL;

			while (interrupt == Interrupt.NULL) { // ciclo de instrucoes. acaba cfe instrucao, veja cada caso.

				ir = m[pc]; // FETCH - busca posicao da memoria apontada por pc, guarda em ir

				// if debug
				showState();
				// EXECUTA INSTRUCAO NO ir
				switch (ir.opc) { // para cada opcode, sua execucao
					// Instrucoes de Memoria
					case LDI: // Rd <- k
						reg[ir.r1] = ir.p;
						pc++;
						break;

					case LDD: // Rd <- [A]
						if (ir.p >= 0 && ir.p <= 1023) {
							reg[ir.r1] = m[ir.p].p;
							pc++;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case LDX: // RD <- [RS] // NOVA
						if (reg[ir.r2] >= 0 && reg[ir.r2] <= 1023) {
							reg[ir.r1] = m[reg[ir.r2]].p;
							pc++;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case STD: // [A] <- Rs
						if (ir.p >= 0 && ir.p <= 1023) {
							m[ir.p].opc = Opcode.DATA;
							m[ir.p].p = reg[ir.r1];
							pc++;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case STX: // [Rd] <- Rs
						if (reg[ir.r1] >= 0 && reg[ir.r1] <= 1023) {
							m[reg[ir.r1]].opc = Opcode.DATA;
							m[reg[ir.r1]].p = reg[ir.r2];
							pc++;
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
						} else {
							interrupt = Interrupt.OVERFLOW;
						}
						break;

					case SUBI: // RD <- RD - k // NOVA
						reg[ir.r1] = reg[ir.r1] - ir.p;
						if (reg[ir.r1] >= -10000 && reg[ir.r1] <= 10000) {
							pc++;
						} else {
							interrupt = Interrupt.OVERFLOW;
						}
						break;

					case ADD: // Rd <- Rd + Rs
						reg[ir.r1] = reg[ir.r1] + reg[ir.r2];
						if (reg[ir.r1] >= -10000 && reg[ir.r1] <= 10000) {
							pc++;
						} else {
							interrupt = Interrupt.OVERFLOW;
						}
						break;

					case SUB: // Rd <- Rd - Rs
						reg[ir.r1] = reg[ir.r1] - reg[ir.r2];
						if (reg[ir.r1] >= -10000 && reg[ir.r1] <= 10000) {
							pc++;
						} else {
							interrupt = Interrupt.OVERFLOW;
						}
						break;

					case MULT: // Rd <- Rd * Rs
						reg[ir.r1] = reg[ir.r1] * reg[ir.r2]; // gera um overflow // --> LIGA INT (1)
						if (reg[ir.r1] >= -10000 && reg[ir.r1] <= 10000) {
							pc++;
						} else {
							interrupt = Interrupt.OVERFLOW;
						}
						break;
					// --------------------------------------------------------------------------------------------------

					// --------------------------------------------------------------------------------------------------
					// Instrucoes JUMP
					case JMP: // PC <- k
						if (ir.p >= 0 && ir.p <= 1023) {
							pc = ir.p;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIGK: // If RC > 0 then PC <- k else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r2] > 0) {
								pc = ir.p;
							} else {
								pc++;
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPILK: // If RC < 0 then PC <- k else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r2] < 0) {
								pc = ir.p;
							} else {
								pc++;
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIEK: // If RC = 0 then PC <- k else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r2] == 0) {
								pc = ir.p;
							} else {
								pc++;
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPI: // PC <- Rs
						if (reg[ir.r1] >= 0 && reg[ir.r1] <= 1023) {
							pc = reg[ir.r1];
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIG: // If RC > 0 then PC<-RS else PC++
						if (reg[ir.r1] >= 0 && reg[ir.r1] <= 1023) {
							if (reg[ir.r2] > 0) {
								pc = reg[ir.r1];
							} else {
								pc++;
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIL: // if Rc < 0 then PC <- Rs Else PC <- PC +1
						if (reg[ir.r1] >= 0 && reg[ir.r1] <= 1023) {
							if (reg[ir.r2] < 0) {
								pc = reg[ir.r1];
							} else {
								pc++;
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIE: // If Rc = 0 Then PC <- Rs Else PC <- PC +1
						if (reg[ir.r1] >= 0 && reg[ir.r1] <= 1023) {
							if (reg[ir.r2] == 0) {
								pc = reg[ir.r1];
							} else {
								pc++;
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIM: // PC <- [A]
						if (ir.p >= 0 && ir.p <= 1023) {
							pc = m[ir.p].p;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}

					case JMPIGM: // If RC > 0 then PC <- [A] else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r2] > 0) {
								pc = m[ir.p].p;
							} else {
								pc++;
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPILM: // If RC < 0 then PC <- k else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r2] < 0) {
								pc = m[ir.p].p;
							} else {
								pc++;
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIEM: // If RC = 0 then PC <- k else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r2] == 0) {
								pc = m[ir.p].p;
							} else {
								pc++;
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case JMPIGT: // If RS>RC then PC <- k else PC++
						if (ir.p >= 0 && ir.p <= 1023) {
							if (reg[ir.r1] > reg[ir.r2]) {
								pc = ir.p;
							} else {
								pc++;
							}
							break;
						} else {
							interrupt = Interrupt.ENDERECO_INVALIDO;
						}
						break;

					case MOVE: // RD <- RS
						reg[ir.r1] = reg[ir.r2];
						pc++;
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
				trataInterrupcao(interrupt);
			}
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

		public VM() {
			// memoria
			tamMem = 1024;
			m = new Word[tamMem]; // m ee a memoria
			for (int i = 0; i < tamMem; i++) {
				m[i] = new Word(Opcode.___, -1, -1, -1);
			}
			;
			// cpu
			cpu = new CPU(m); // cpu acessa memoria
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
				m[i].opc = p[i].opc;
				m[i].r1 = p[i].r1;
				m[i].r2 = p[i].r2;
				m[i].p = p[i].p;
			}
		}

		public void executa() {
			vm.cpu.setContext(0); // monitor seta contexto - pc aponta para inicio do programa
			vm.cpu.run(); // e cpu executa
							// note aqui que o monitor espera que o programa carregado acabe normalmente
							// nao ha protecoes... o que poderia acontecer ?
		}

	}
	
	public class GerenteDeMemoria {				
			int tamMem = vm.tamMem;			
			int tamPag = 16;
			int tamFrame = tamPag;
			int nroFrames = tamMem/tamPag;
			
			Word[] mem = new Word[tamMem];
			boolean[] frames = new boolean[nroFrames]; // if TRUE=ocupado, if FALSE=livre
			int[] tabelaPaginas = new int[nroFrames];
			int tamTabelaPaginas = 0;
			
			public GerenteDeMemoria() { 
					for (int x = 0; x < tamMem; x++) {
						mem[x] = new Word(Opcode.___, -1, -1, -1);
					}
				
					for (int i = 0; i < nroFrames; i++) {
						frames[i] = false;
					}	
					
					for (int i = 0; i < nroFrames; i++) {
						tabelaPaginas[i] = 0;
					}
			}
			
			public int[] aloca(int nroPalavras) {
				
				System.out.print("aloca ");
				System.out.print(nroPalavras);
				System.out.println(" palavras");
				
				double localNroPalavras = nroPalavras;
				
				double nroPaginas = localNroPalavras/16;
				System.out.print("NUMERO PAGINAS APOS DIVISAO: ");
				System.out.println(nroPaginas);
				
				if(nroPaginas>Math.floor(nroPaginas)) {
					// SE FOR UM VALOR QUEBRADO, ARREDONDA PARA CIMA
					nroPaginas = Math.ceil(nroPaginas);
				} else {
					// SE FOR UM VALOR INTEIRO NAO FAZ NADA
				}

				double nroFrames = nroPaginas;
				System.out.print("VAI PRECISAR DE ");
				System.out.print(nroFrames);
				System.out.println(" FRAMES");
				
				
				boolean verificaAlocacao = verificaSePodeAlocar(nroFrames);
				if(verificaAlocacao == true) {
					System.out.println("verifica alocacao true");
					System.out.print("Nro frames: ");
					System.out.println(nroFrames);
					
					for(int i=0; i<nroFrames; i++) {
						System.out.print("i=");
						System.out.println(i);
						
						if(frames[i] == true) { // se ocupado avanca no vetor
							System.out.println("frame ocupado");
							nroFrames++;
						} else {
							System.out.println("frame livre");
							frames[i]=true;	// se livre, ocupa e adiciona na tabela de paginas
							System.out.print("OCUPANDO O FRAME ");
							System.out.println(i);
							
							//printaFrame();

							tabelaPaginas[tamTabelaPaginas] = i;
							tamTabelaPaginas++;
						}	
					}
					System.out.print("Tamanho tab pag: ");
					System.out.println(tamTabelaPaginas);
					printaTabelaPaginas(tamTabelaPaginas);
					return tabelaPaginas;
				} else {
					System.out.println("verifica alocacao false");
					return tabelaPaginas;
				}			
				
			}
			
			public int contaFramesLivres() {
				System.out.println("conta frames livres");
				int contaFramesLivres = 0;
				for (int i = 0; i < nroFrames; i++) {
					if(frames[i] == false) {
						contaFramesLivres++;
					}	
				}
				System.out.print("conta frames livres: ");
				System.out.println(contaFramesLivres);
				return contaFramesLivres;
			}
			
			public boolean verificaSePodeAlocar(double nroFrames) {
				System.out.print("verifica se pode alocar ");
				System.out.print(nroFrames);
				System.out.println(" frame(s)");
				double framesLivres = contaFramesLivres();
				System.out.print("frames livres: ");
				System.out.println(framesLivres);
				
				if((framesLivres-nroFrames) > 0) {
					return true;
				} else {
					return false;
				}	
			}
			
			public void printaFrame() {
				for(int i=0; i<nroFrames ; i++) {
					System.out.print("[");
					System.out.print(i);
					System.out.print("] :");
					System.out.println(frames[i]);
				}
			}
			
			public void printaTabelaPaginas(int n) {
				System.out.println("TABELA DE PAGINAS:");
				for(int i=0; i<n ; i++) {
					System.out.print("[");
					System.out.print(i);
					System.out.print("] :");
					System.out.println(tabelaPaginas[i]);
				}
			}
			
			
			
	}
	// -------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// ------------------- S I S T E M A
	// --------------------------------------------------------------------

	public VM vm;
	public Monitor monitor;
	public GerenteDeMemoria gerenteMemoria;
	public static Programas progs;

	public Sistema() { // a VM com tratamento de interrupcoes
		vm = new VM();
		monitor = new Monitor();
		gerenteMemoria = new GerenteDeMemoria();
		progs = new Programas();
	}

	public void roda(Word[] programa) {
		monitor.carga(programa, vm.m);
		System.out.println("---------------------------------- programa carregado ");
		monitor.dump(vm.m, 0, programa.length);
		monitor.executa();
		System.out.println("---------------------------------- apos execucao ");
		monitor.dump(vm.m, 0, programa.length);
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

	public void trataInterrupcao(Interrupt interrupt) {
		switch (interrupt) {
			case STOP:
				System.out.print("\n");
				System.out.print("\n");
				System.out.println(">>> System Interruption: FINAL DO PROGRAMA <<<");
				System.out.print("\n");
				System.out.print("\n");
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
	
public void chamaSistema() {
	
	CPU cpuAccess = vm.cpu;
	
	if (cpuAccess.reg[8] == 1) { // IN
		int valorLido = leInteiro(); // Chama o metodo que le um inteiro do teclado
		if (valorLido >= -10000 && valorLido <= 10000) { // Verifica se esta dentro do range valido
			cpuAccess.m[cpuAccess.reg[9]].p = valorLido; // Coloca o valor lido no endereco de mem. armazenado no reg. 9
			cpuAccess.pc++;
		} else {
			interrupt = Interrupt.OVERFLOW; // Se estiver fora do range interrompe por overflow
		}

	} else if (cpuAccess.reg[8] == 2) { // OUT
		int valorOut = cpuAccess.m[cpuAccess.reg[9]].p; // O endereco de mem. cujo valor deve-se escrever na tela esta
									// armazenado no reg. 9
		escreveIntTela(valorOut); // Chama o metodo que escreve um inteiro na tela
		cpuAccess.pc++;
	} else {
		// Caso hajam novas chamadas de sistema
	}
		
}

	// ------------------- S I S T E M A - fim
	// --------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// ------------------- instancia e testa sistema
	public static void main(String args[]) {
		Sistema s = new Sistema();
		// s.roda(progs.fibonacci10); // "progs" significa acesso/referencia ao programa
		// em memoria secundaria
		// s.roda(progs.progMinimo);
		// s.roda(progs.fatorial);
		// s.roda(progs.NewInstructionTester);
		// s.roda(progs.PA);
		// s.roda(progs.PB);
		// s.roda(progs.PC);
		//s.roda(progs.InterruptionTester);
		//s.roda(progs.SystemCallTester);
		
		s.gerenteMemoria.aloca(150);
		s.gerenteMemoria.aloca(30);
		s.gerenteMemoria.aloca(10);
		
		

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

		public Word[] SystemCallTester = new Word[] { new Word(Opcode.JMP, -1, -1, 9), // 0 // Inicia o prog. na pos. 9
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
