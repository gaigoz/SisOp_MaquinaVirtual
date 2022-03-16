// PUCRS - Escola PolitÃ©cnica - Sistemas Operacionais
// Prof. Fernando Dotti
// CÃ³digo fornecido como parte da soluÃ§Ã£o do projeto de Sistemas Operacionais
//
// Fase 1 - mÃ¡quina virtual (vide enunciado correspondente)
//

import java.util.*;
public class Sistema {
	
	// -------------------------------------------------------------------------------------------------------
	// --------------------- H A R D W A R E - definicoes de HW ---------------------------------------------- 

	// -------------------------------------------------------------------------------------------------------
	// --------------------- M E M O R I A -  definicoes de opcode e palavra de memoria ---------------------- 
	
	public class Word { 	// cada posicao da memoria tem uma instrucao (ou um dado)
		public Opcode opc; 	//
		public int r1; 		// indice do primeiro registrador da operacao (Rs ou Rd cfe opcode na tabela)
		public int r2; 		// indice do segundo registrador da operacao (Rc ou Rs cfe operacao)
		public int p; 		// parametro para instrucao (k ou A cfe operacao), ou o dado, se opcode = DADO

		public Word(Opcode _opc, int _r1, int _r2, int _p) {  
			opc = _opc;   r1 = _r1;    r2 = _r2;	p = _p;
		}
	}
    // -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
    // --------------------- C P U  -  definicoes da CPU ----------------------------------------------------- 

	public enum Opcode {
		DATA, ___,		    // se memoria nesta posicao tem um dado, usa DATA, se nao usada ee NULO ___
		JMP, JMPI, JMPIG, JMPIL, JMPIE,  JMPIM, JMPIGM, JMPILM, JMPIEM, STOP,   // desvios e parada
		ADDI, SUBI,  ADD, SUB, MULT,         // matematicos
		LDI, LDD, STD, LDX, STX, SWAP;        // movimentacao
	}

	public class CPU {
							// caracterÃ­stica do processador: contexto da CPU ...
		private int pc; 			// ... composto de program counter,
		private Word ir; 			// instruction register,
		private int[] reg;       	// registradores da CPU

		private Word[] m;   // CPU acessa MEMORIA, guarda referencia 'm' a ela. memoria nao muda. ee sempre a mesma.
			
		public CPU(Word[] _m) {     // ref a MEMORIA e interrupt handler passada na criacao da CPU
			m = _m; 				// usa o atributo 'm' para acessar a memoria.
			reg = new int[8]; 		// aloca o espaÃ§o dos registradores
		}

		public void setContext(int _pc) {  // no futuro esta funcao vai ter que ser 
			pc = _pc;                                              // limite e pc (deve ser zero nesta versao)
		}
	
		private void dump(Word w) {
			System.out.print("[ "); 
			System.out.print(w.opc); System.out.print(", ");
			System.out.print(w.r1);  System.out.print(", ");
			System.out.print(w.r2);  System.out.print(", ");
			System.out.print(w.p);  System.out.println("  ] ");
		}

        private void showState(){
			 System.out.println("       "+ pc); 
			   System.out.print("           ");
			 for (int i=0; i<8; i++) { System.out.print("r"+i);   System.out.print(": "+reg[i]+"     "); };  
			 System.out.println("");
			 System.out.print("           ");  dump(ir);
		}

		public void run() { 		// execucao da CPU supoe que o contexto da CPU, vide acima, esta devidamente setado
			while (true) { 			// ciclo de instrucoes. acaba cfe instrucao, veja cada caso.
				// FETCH
					ir = m[pc]; 	// busca posicao da memoria apontada por pc, guarda em ir
					//if debug
					    showState();
				// EXECUTA INSTRUCAO NO ir
					switch (ir.opc) { // para cada opcode, sua execuÃ§Ã£o

						case LDI: // Rd â†� k
							reg[ir.r1] = ir.p;
							pc++;
							break;
							
						case LDD: // Rd <- [A]				// Instrução nova - pode não estar funcionando corretamente
							reg[ir.r1] = m[ir.p].p; 
							pc++;
							break;
							
						case LDX: // RD <- [RS]				// Instrução nova - pode não estar funcionando corretamente
							reg[ir.r1] = reg[ir.r2];
							pc++;
							break;
							
						case STD: // [A] <- Rs
							m[ir.p].opc = Opcode.DATA;
							m[ir.p].p = reg[ir.r1];
							pc++;
							break;

						case ADD: // Rd â†� Rd + Rs
							reg[ir.r1] = reg[ir.r1] + reg[ir.r2];
							pc++;
							break;

						case MULT: // Rd â†� Rd * Rs
							reg[ir.r1] = reg[ir.r1] * reg[ir.r2];
							// gera um overflow
							// -->  LIGA INT  (1)
							pc++;
							break;

						case ADDI: // Rd â†� Rd + k
							reg[ir.r1] = reg[ir.r1] + ir.p;
							pc++;
							break;

						case STX: // [Rd] <- Rs
							    m[reg[ir.r1]].opc = Opcode.DATA;      
							    m[reg[ir.r1]].p = reg[ir.r2];          
								pc++;
							break;

						case SUB: // Rd â†� Rd - Rs
							reg[ir.r1] = reg[ir.r1] - reg[ir.r2];
							pc++;
							break;
							
						case SUBI: // RD <- RD - k				// Instrução nova - pode não estar funcionando corretamente
							reg[ir.r1] = reg[ir.r1] - ir.p;
							pc++;
							break;	

						case JMP: //  PC â†� k
							pc = ir.p;
						    break;
						
						case JMPIG: // If RC > 0 then PC<-RS else PC++
							if (reg[ir.r2] > 0) {
								pc = reg[ir.r1];
							} else {
								pc++;
							}
							break;
							
						case JMPIGM: // If RC > 0 then PC <- [A] else PC++		// Instrução nova - pode não estar funcionando corretamente
							if (reg[ir.r2] > 0) {
								pc = ir.p;
							} else {
								pc++;
							}
							break;

						case JMPIE: // If Rc = 0 Then PC â†� Rs Else PC â†� PC +1
							if (reg[ir.r2] == 0) {
								pc = reg[ir.r1];
							} else {
								pc++;
							}
							break;
							
						case JMPIEM: // If RC = 0 then PC <- k else PC++  	// Instrução nova - pode não estar funcionando corretamente
							if (reg[ir.r2] == 0) {
								pc = ir.p;
							} else {
								pc++;
							}
							break;
							
						case JMPILM: // If RC < 0 then PC <- k else PC++	// Instrução nova - pode não estar funcionando corretamente
							if (reg[ir.r2] < 0) {
								pc = ir.p;
							} else {
								pc++;
							}
							break;

						case STOP: // por enquanto, para execucao
							break;
						default:
						    // opcode desconhecido
							// liga interrup (2)
					}
				
				// VERIFICA INTERRUPÃ‡ÃƒO !!! - TERCEIRA FASE DO CICLO DE INSTRUÃ‡Ã•ES
				if (ir.opc==Opcode.STOP) {   
					break; // break sai do loop da cpu

			    // if int ligada - vai para tratamento da int 
				//     desviar para rotina java que trata int
				}
			}
		}
	}
    // ------------------ C P U - fim ------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	
    // ------------------- V M  - constituida de CPU e MEMORIA -----------------------------------------------
    // -------------------------- atributos e construcao da VM -----------------------------------------------
	public class VM {
		public int tamMem;    
        public Word[] m;     
        public CPU cpu;    

        public VM(){    
	     // memÃ³ria
  		 	 tamMem = 1024;
			 m = new Word[tamMem]; // m ee a memoria
			 for (int i=0; i<tamMem; i++) { m[i] = new Word(Opcode.___,-1,-1,-1); };
	  	 // cpu
			 cpu = new CPU(m);   // cpu acessa memÃ³ria
	    }	
	}
    // ------------------- V M  - fim ------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

    // --------------------H A R D W A R E - fim -------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------
	// ------------------- S O F T W A R E - inicio ----------------------------------------------------------

		// -------------------------------------------  funcoes de um monitor
	public class Monitor {
			public void dump(Word w) {
				System.out.print("[ "); 
				System.out.print(w.opc); System.out.print(", ");
				System.out.print(w.r1);  System.out.print(", ");
				System.out.print(w.r2);  System.out.print(", ");
				System.out.print(w.p);  System.out.println("  ] ");
			}
			public void dump(Word[] m, int ini, int fim) {
				for (int i = ini; i < fim; i++) {
					System.out.print(i); System.out.print(":  ");  dump(m[i]);
				}
			}
			public void carga(Word[] p, Word[] m) {    // significa ler "p" de memoria secundaria e colocar na principal "m"
				for (int i = 0; i < p.length; i++) {
					m[i].opc = p[i].opc;     m[i].r1 = p[i].r1;     m[i].r2 = p[i].r2;     m[i].p = p[i].p;
				}
			}
			public void executa() {          
				vm.cpu.setContext(0);          // monitor seta contexto - pc aponta para inicio do programa 
				vm.cpu.run();                  //                         e cpu executa
				                               // note aqui que o monitor espera que o programa carregado acabe normalmente
											   // nao ha protecoes...  o que poderia acontecer ?
				}
		}
	   // -------------------------------------------  
		



	// -------------------------------------------------------------------------------------------------------
    // -------------------  S I S T E M A --------------------------------------------------------------------

	public VM vm;
	public Monitor monitor;
	public static Programas progs;

    public Sistema(){   // a VM com tratamento de interrupÃ§Ãµes
		 vm = new VM();
		 monitor = new Monitor();
		 progs = new Programas(); 
	}

	public void roda(Word[] programa){
			monitor.carga(programa, vm.m);    
			System.out.println("---------------------------------- programa carregado ");
			monitor.dump(vm.m, 0, programa.length);
			monitor.executa();        
			System.out.println("---------------------------------- apÃ³s execucao ");
			monitor.dump(vm.m, 0, programa.length);
		}

    // -------------------  S I S T E M A - fim --------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------------------------
    // ------------------- instancia e testa sistema
	public static void main(String args[]) {
		Sistema s = new Sistema();
	    //s.roda(progs.fibonacci10);           // "progs" significa acesso/referencia ao programa em memoria secundaria
		//s.roda(progs.progMinimo);
		//s.roda(progs.fatorial);
		s.roda(progs.NewInstructionTester);
	    //s.roda(progs.PA);
		//s.roda(progs.PB);
	}
    // -------------------------------------------------------------------------------------------------------
    // --------------- TUDO ABAIXO DE MAIN Ã‰ AUXILIAR PARA FUNCIONAMENTO DO SISTEMA - nao faz parte 

   //  -------------------------------------------- programas aa disposicao para copiar na memoria (vide carga)
   public class Programas {
	   public Word[] progMinimo = new Word[] {
		    //       OPCODE      R1  R2  P         :: VEJA AS COLUNAS VERMELHAS DA TABELA DE DEFINICAO DE OPERACOES
			//                                     :: -1 SIGNIFICA QUE O PARAMETRO NAO EXISTE PARA A OPERACAO DEFINIDA
		    new Word(Opcode.LDI, 0, -1, 999), 		
			new Word(Opcode.STD, 0, -1, 10), 
			new Word(Opcode.STD, 0, -1, 11), 
			new Word(Opcode.STD, 0, -1, 12), 
			new Word(Opcode.STD, 0, -1, 13), 
			new Word(Opcode.STD, 0, -1, 14), 
			new Word(Opcode.STOP, -1, -1, -1) };

	   public Word[] fibonacci10 = new Word[] { // mesmo que prog exemplo, so que usa r0 no lugar de r8
			new Word(Opcode.LDI, 1, -1, 0), 
			new Word(Opcode.STD, 1, -1, 20),    // 20 posicao de memoria onde inicia a serie de fibonacci gerada  
			new Word(Opcode.LDI, 2, -1, 1),
			new Word(Opcode.STD, 2, -1, 21),      
			new Word(Opcode.LDI, 0, -1, 22),       
			new Word(Opcode.LDI, 6, -1, 6),
			new Word(Opcode.LDI, 7, -1, 30),       
			new Word(Opcode.LDI, 3, -1, 0), 
			new Word(Opcode.ADD, 3, 1, -1),
			new Word(Opcode.LDI, 1, -1, 0), 
			new Word(Opcode.ADD, 1, 2, -1), 
			new Word(Opcode.ADD, 2, 3, -1),
			new Word(Opcode.STX, 0, 2, -1), 
			new Word(Opcode.ADDI, 0, -1, 1), 
			new Word(Opcode.SUB, 7, 0, -1),
			new Word(Opcode.JMPIG, 6, 7, -1), 
			new Word(Opcode.STOP, -1, -1, -1),   // POS 16
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),   // POS 20
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1)  // ate aqui - serie de fibonacci ficara armazenada
			   };   

	   public Word[] fatorial = new Word[] { 	 // este fatorial so aceita valores positivos.   nao pode ser zero
												 // linha   coment
			new Word(Opcode.LDI, 0, -1, 6),      // 0   	r0 Ã© valor a calcular fatorial
			new Word(Opcode.LDI, 1, -1, 1),      // 1   	r1 Ã© 1 para multiplicar (por r0)
			new Word(Opcode.LDI, 6, -1, 1),      // 2   	r6 Ã© 1 para ser o decremento
			new Word(Opcode.LDI, 7, -1, 8),      // 3   	r7 tem posicao de stop do programa = 8
			new Word(Opcode.JMPIE, 7, 0, 0),     // 4   	se r0=0 pula para r7(=8)
			new Word(Opcode.MULT, 1, 0, -1),     // 5   	r1 = r1 * r0
			new Word(Opcode.SUB, 0, 6, -1),      // 6   	decrementa r0 1 
			new Word(Opcode.JMP, -1, -1, 4),     // 7   	vai p posicao 4
			new Word(Opcode.STD, 1, -1, 10),     // 8   	coloca valor de r1 na posiÃ§Ã£o 10
			new Word(Opcode.STOP, -1, -1, -1),    // 9   	stop
			new Word(Opcode.DATA, -1, -1, -1) };  // 10   ao final o valor do fatorial estarÃ¡ na posiÃ§Ã£o 10 da memÃ³ria
	   
	   public Word[] PA = new Word[] { 			    // Reutiliza  o código do programa "fibonacci10", que é apenas incrementado
			    new Word(Opcode.LDD, 4, -1, 31),    // 0     Carrega no registrador R4 o valor da pos. 31 de mem. (valor base para lógica do programa)
			    new Word(Opcode.JMPIGM, -1, 4, 5),  // 1     Se maior que 0 pula para linha 5 do código
			     new Word(Opcode.LDI, 1, -1, -1),	// 2     Se não, coloca -1 na primeira pos. de mem. de saída(pos. 33)
			     new Word(Opcode.STD, 1, -1, 20),	// 3
			     new Word(Opcode.STOP, -1, -1, -1), // 4     Finaliza a execução do programa
			    
				new Word(Opcode.LDI, 1, -1, 0), 	// 5	 Gera o primeiro valor da sequencia de fibonacci (0)
				new Word(Opcode.STD, 1, -1, 33),    // 6     Salva na primeira pos. de mem. destinada a sequencia (pos. 33)
				 new Word(Opcode.SUBI, 4, -1, 1),	// 7     Faz R4-1, pois R4 armazenava o numero total de valores desejados, utilizaremos R4 como index	
				 new Word(Opcode.JMPIGM, -1, 4, 10),// 8     Finaliza se for igual a 0, se não, segue a execução
				  new Word(Opcode.STOP, -1, -1, -1),// 9
				 
				new Word(Opcode.LDI, 2, -1, 1),		// 10	Gera o segundo valor da sequencia de fibonacci (1)
				new Word(Opcode.STD, 2, -1, 34),    // 11	Salva na segunda pos. de mem. destinada a sequencia (pos. 34)
				 new Word(Opcode.SUBI, 4, -1, 1),	// 12   Faz R4-1	
				 new Word(Opcode.JMPIGM, -1, 4, 15),// 13   Finaliza se for igual a 0, se não, segue a execução
				  new Word(Opcode.STOP, -1, -1, -1),// 14
				 
				new Word(Opcode.LDI, 0, -1, 35),    // 15	A partir daqui, calcularemos matematicamente os próximos valores da sequencia da pos. 35 da mem. em diante
				new Word(Opcode.LDI, 7, -1, 1),		// 16
				new Word(Opcode.LDI, 6, -1, 19),	// 17   19 é a pos. de mem. do inicio do loop que faz o cálculo de novos valores
				new Word(Opcode.LDX, 7, 4, -1),		// 18	Faz R7<-[R4] (R4 tem o núm. de valores da sequencia que faltam ser calculados)
				 new Word(Opcode.JMPIEM, -1, 7, 29),// 19   Sai do loop quando R7 zera (R7 tem o núm. de valores da sequencia que faltam ser calculados)       
				new Word(Opcode.LDI, 3, -1, 0), 	// 20
				new Word(Opcode.ADD, 3, 1, -1),		// 21
				new Word(Opcode.LDI, 1, -1, 0), 	// 22
				new Word(Opcode.ADD, 1, 2, -1), 	// 23
				new Word(Opcode.ADD, 2, 3, -1),		// 24
				new Word(Opcode.STX, 0, 2, -1), 	// 25
				new Word(Opcode.ADDI, 0, -1, 1), 	// 26
				new Word(Opcode.SUBI, 7, -1, 1),	// 27
				new Word(Opcode.JMPIG, 6, 7, -1), 	// 28	Volta para o início do loop
				new Word(Opcode.STOP, -1, -1, -1),  // 29	Termina a execução
				// Área de dados
				new Word(Opcode.DATA, -1, -1, -1),	// 30
				new Word(Opcode.DATA, -1, -1, 10),	// 31   <----- Se o valor armazenado nesta pos. de mem. for menor que 0, coloca -1 no início da pos. de mem. para saída (pos 25). Se for maior, este é o núm. de valores da sequência
				new Word(Opcode.DATA, -1, -1, -1),	// 32
				new Word(Opcode.DATA, -1, -1, -1),  // 33	Sequencia salva desta posição.....
				new Word(Opcode.DATA, -1, -1, -1),	// 34
				new Word(Opcode.DATA, -1, -1, -1),	// 35
				new Word(Opcode.DATA, -1, -1, -1),	// 36
				new Word(Opcode.DATA, -1, -1, -1),	// 37
				new Word(Opcode.DATA, -1, -1, -1),	// 38
				new Word(Opcode.DATA, -1, -1, -1),	// 39
				new Word(Opcode.DATA, -1, -1, -1),	// 40
				new Word(Opcode.DATA, -1, -1, -1),	// 41
				new Word(Opcode.DATA, -1, -1, -1)   // 42    ...ate esta
			};
	   
	   public Word[] PB = new Word[] { 	 // Reutiliza  o código do programa "fatorial", que é apenas incrementado
			   new Word(Opcode.LDD, 0, -1, 14),     // 0   	Carrega no registrador 0 o valor da pos. 10 de mem. (valor base para lógica do programa)
			   
			   new Word(Opcode.JMPILM, -1, 0, 11),	// 1	Se R0<0, pula para a linha 11 do código
				  
			   new Word(Opcode.LDI, 1, -1, 1),      // 2   
			   new Word(Opcode.LDI, 6, -1, 1),      // 3   	
			   new Word(Opcode.LDI, 7, -1, 8),      // 4   	
			   new Word(Opcode.JMPIE, 7, 0, 0),     // 5   	
			   new Word(Opcode.MULT, 1, 0, -1),     // 6   	
			   new Word(Opcode.SUB, 0, 6, -1),      // 7   	 
			   new Word(Opcode.JMP, -1, -1, 4),     // 8   
			   new Word(Opcode.STD, 1, -1, 15),     // 9   
			   new Word(Opcode.STOP, -1, -1, -1),   // 10   Finaliza a execução do programa
			   
			   new Word(Opcode.LDI, 1, -1, -1),		// 11	
			   new Word(Opcode.STD, 1, -1, 15),		// 12	Salva o valor -1 na pos. de mem. destinada ao output da resposta (pos. 15)
			   new Word(Opcode.STOP, -1, -1, -1),	// 13	Finaliza a execução do programa
			   // Área de dados
			   new Word(Opcode.DATA, -1, -1, 6), 	// 14   <---- Valor a se calcular o fatorial
			   new Word(Opcode.DATA, -1, -1, -1)	// 15	Valor do fatorial calculado estara nesa pos. de mem.
		};  
	   
	   public Word[] NewInstructionTester = new Word[] { 	// Testa as novas instruções implementadas
			   new Word(Opcode.JMP, -1, -1, 14),	// 0	// Inicia o prog. na pos. 14 da mem.
			   // Área de dados
			   new Word(Opcode.DATA, -1, -1, 200),	// 1  - Ocupada
			   new Word(Opcode.DATA, -1, -1, -1),	// 2  - Ocupada
			   new Word(Opcode.DATA, -1, -1, -1),	// 3  - Ocupada
			   new Word(Opcode.DATA, -1, -1, -1),	// 4  - Livre
			   new Word(Opcode.DATA, -1, -1, -1),	// 5  - Livre
			   new Word(Opcode.DATA, -1, -1, -1),	// 6  - Livre
			   new Word(Opcode.DATA, -1, -1, -1),	// 7  - Livre
			   new Word(Opcode.DATA, -1, -1, -1),	// 8  - Livre
			   new Word(Opcode.DATA, -1, -1, -1),	// 9  - Livre
			   new Word(Opcode.DATA, -1, -1, -1),	// 10 - Livre
			   new Word(Opcode.DATA, -1, -1, -1),	// 11 - Livre
			   new Word(Opcode.DATA, -1, -1, -1),	// 12 - Livre
			   new Word(Opcode.DATA, -1, -1, -1),	// 13 - Livre
			   // Programa
			   new Word(Opcode.LDD, 4, -1, 1),      // 14	LDD - FUNCIONANDO (se insere em R4 o valor da pos. 1 da mem.)
			   
			   new Word(Opcode.JMPIGM, -1, 4, 17),	// 15	JMPIGM - FUNCIONANDO (se salva corretamente o valor de R4 na pos. 2 da mem.)
			    new Word(Opcode.LDI, 4, -1, 100),	// 16
			   new Word(Opcode.STD, 4, -1, 2),		// 17
			   
			   new Word(Opcode.LDX, 1, 4, -1),		// 18	LDX - FUNCIONANDO (se insere em R1 o valor de R4)
			   
			   new Word(Opcode.SUBI, 4, -1, 10),	// 19	SUBI - FUNCIONANDO (se R4 passa a valer R4-10)
			   
			   new Word(Opcode.LDI, 3, -1, 0),		// 20
			   new Word(Opcode.JMPIEM, -1, 3, 23),  // 21	JMPIEM - FUNCIONANDO (funcionando se salva o valor 0 na pos. 3 da mem.)
			    new Word(Opcode.LDI, 3, -1, 100),	// 22
			   new Word(Opcode.STD, 3, -1, 2),		// 23
			   
			   new Word(Opcode.LDI, 3, -1, 0),		// 24
			   new Word(Opcode.JMPILM, -1, 3, 27),	// 25	JMPILM - FUNCIONANDO (funcionando se salva o valor 100 na pos. 4 da mem.)
			    new Word(Opcode.LDI, 3, -1, 100),	// 26
			   new Word(Opcode.STD, 3, -1, 4),		// 27
			    
			   new Word(Opcode.STOP, -1, -1, -1)	//
				   };
    }
}

