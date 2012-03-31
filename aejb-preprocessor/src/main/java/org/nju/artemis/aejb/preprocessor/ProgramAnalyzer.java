package org.nju.artemis.aejb.preprocessor;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.util.TraceClassVisitor;
/**
 * 
 * 
 * @author <a href="mailto:njupsu@gmail.com">Su Ping</a>
 */
public class ProgramAnalyzer {
	//存放分析程序所要调用的EJB
	private List<String> ejbs=new LinkedList();
	//存放状态机信息，主要是分析程序的控制流
	StateMachine stateMachine=new StateMachine();
	//存放每个控制流程的具体信息，即si-ai-sj
	ControlFlow controlflow=new ControlFlow();
	//每个状态点已使用的对外调用
	List[] past;
	//每个状态点将要的对外调用
	List[] future;
	//对于已分析出的跳转及在改点要加入的跳转信息
	Hashtable<AbstractInsnNode, String> jumpinf = new Hashtable<AbstractInsnNode, String>();
	//分析出对应每个状态的所以可能的下步动作及所到达的下个状态
	List<String> next=new LinkedList();
	//分析出的所以状态信息
	List<String> stateall=new LinkedList();

	//字节码中插入transaction、触发trigger等信息
	public void transform(ClassNode cn) {
		if ((cn.access & ACC_INTERFACE) == 0) {
			setEjbs(cn);
			// System.out.println("catch class!"+cn.name);
			for (MethodNode mn : (List<MethodNode>) cn.methods) {
				// System.out.println("catch class!"+mn.name);
				if (isTransaction(mn, "Ljavax/aejb/Transaction;")) {
					// System.out.println("begin analyze!");
					InsnList insns = mn.instructions;
					stateMachine.setStart(0);
					stateMachine.setEnd(mn.instructions.size());
					try {
						ExtractControlFlow(cn.name, mn);
					} catch (AnalyzerException ignored) {
					}
					recognize_state(0, 0, mn.instructions);
					mergeState();
					ExtractMetaData();
					setNext();
					// 字节码文件中插入抽取地状态及状态变化触发信息
					Iterator<AnnotationNode> iter = mn.visibleAnnotations
							.iterator();
					while (iter.hasNext()) {
						AnnotationNode an = iter.next();
						if (an.desc.equals("Ljavax/aejb/Transaction;")) {
							System.out.println(an.values);
							List<Object> valuenew = new LinkedList<Object>();
							valuenew.add("name");
							valuenew.add(mn.name);
							valuenew.add("states");
							valuenew.add(stateall);
							valuenew.add("next");
							valuenew.add(next);
							an.values = valuenew;
						}
					}

					LabelNode lb0 = new LabelNode();
					LabelNode lb1 = new LabelNode();
					LabelNode lb2 = new LabelNode();
					LabelNode lb3 = new LabelNode();
					LabelNode lb4 = new LabelNode();
					LabelNode lb5 = new LabelNode();
					LabelNode lb6 = new LabelNode();
					mn.tryCatchBlocks.add(new TryCatchBlockNode(lb0, lb1, lb2,
							"javax/naming/NamingException"));

					InsnList begin = new InsnList();
					int localnum = mn.localVariables.size();
					begin.add(lb3);
					begin.add(new InsnNode(ACONST_NULL));
					begin.add(new VarInsnNode(ASTORE, localnum));
					begin.add(lb0);
					begin.add(new TypeInsnNode(NEW,
							"javax/naming/InitialContext"));
					begin.add(new InsnNode(DUP));
					begin.add(new MethodInsnNode(INVOKESPECIAL,
							"javax/naming/InitialContext", "<init>", "()V"));
					begin.add(new VarInsnNode(ASTORE, localnum + 1));

					begin.add(lb4);
					begin.add(new VarInsnNode(ALOAD, localnum + 1));
					begin.add(new LdcInsnNode(
							"java:global/aejb/transactionmanager/" + mn.name));
					begin.add(new MethodInsnNode(INVOKEINTERFACE,
							"javax/naming/Context", "lookup",
							"(Ljava/lang/String;)Ljava/lang/Object;"));
					begin.add(new TypeInsnNode(CHECKCAST,
							"javax/aejb/TransactionTrigger"));
					begin.add(new VarInsnNode(ASTORE, localnum));
					begin.add(lb1);
					begin.add(new JumpInsnNode(GOTO, lb5));
					begin.add(lb2);
					begin.add(new FrameNode(Opcodes.F_FULL, 2, new Object[] {
							mn.name, "javax/aejb/TransactionTrigger" }, 1,
							new Object[] { "javax/naming/NamingException" }));
					begin.add(new VarInsnNode(ASTORE, localnum + 1));

					begin.add(lb6);
					begin.add(new VarInsnNode(ALOAD, localnum + 1));
					begin.add(new MethodInsnNode(INVOKEVIRTUAL,
							"javax/naming/NamingException", "printStackTrace",
							"()V"));
					begin.add(lb5);

					Iterator<AbstractInsnNode> i = insns.iterator();
					int src = 0;
					while (i.hasNext()) {
						AbstractInsnNode i1 = i.next();
						if (i1 instanceof MethodInsnNode) {
							String owner = ((MethodInsnNode) i1).owner;
							// System.out.println(((MethodInsnNode)
							// i1).owner+","+((MethodInsnNode)i1).name+","+((MethodInsnNode)
							// i1).desc);
							if (ejbs.contains(owner)) {
								String e = "Ejb." + owner + "."
										+ ((MethodInsnNode) i1).name;
								InsnList il = new InsnList();
								il.add(new TypeInsnNode(NEW,
										"java/lang/StringBuilder"));
								il.add(new InsnNode(DUP));
								il.add(new LdcInsnNode(mn.name + "/"));
								il.add(new MethodInsnNode(INVOKESPECIAL,
										"java/lang/StringBuilder", "<init>",
										"(Ljava/lang/String;)V"));
								il.add(new VarInsnNode(ALOAD, 0));
								il.add(new MethodInsnNode(INVOKEVIRTUAL,
										"java/lang/Object", "toString",
										"()Ljava/lang/String;"));
								il.add(new MethodInsnNode(INVOKEVIRTUAL,
										"java/lang/StringBuilder", "append",
										"(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
								il.add(new MethodInsnNode(INVOKEVIRTUAL,
										"java/lang/StringBuilder", "toString",
										"()Ljava/lang/String;"));
								insns.insert(i1.getPrevious(), il);
								// System.out.println(((MethodInsnNode)
								// i1).desc+"111"+((MethodInsnNode)
								// i1).desc.replace("(",
								// "(Ljava/lang/String;"));
								InsnList trig = new InsnList();
								trig.add(new VarInsnNode(ALOAD, localnum));
								trig.add(new VarInsnNode(ALOAD, 0));
								trig.add(new MethodInsnNode(INVOKEVIRTUAL,
										"java/lang/Object", "toString",
										"()Ljava/lang/String;"));
								trig.add(new LdcInsnNode(e));
								trig.add(new MethodInsnNode(INVOKEVIRTUAL,
										"javax/aejb/TransactionTrigger",
										"trigger",
										"(Ljava/lang/String;Ljava/lang/String;)V"));
								if (i1.getNext().getOpcode() == ISTORE) {
									insns.insert(i1.getNext(), trig);
								} else {
									insns.insert(i1, trig);
								}
								insns.insert(
										i1.getPrevious(),
										new MethodInsnNode(
												INVOKEVIRTUAL,
												((MethodInsnNode) i1).owner,
												((MethodInsnNode) i1).name,
												((MethodInsnNode) i1).desc
														.replace("(",
																"(Ljava/lang/String;")));
								insns.remove(i1);
							}

						} else {
							if (jumpinf.containsKey(i1)) {
								InsnList trig = new InsnList();
								trig.add(new VarInsnNode(ALOAD, localnum));
								trig.add(new VarInsnNode(ALOAD, 0));
								trig.add(new MethodInsnNode(INVOKEVIRTUAL,
										"java/lang/Object", "toString",
										"()Ljava/lang/String;"));
								trig.add(new LdcInsnNode(jumpinf.get(i1)));
								trig.add(new MethodInsnNode(INVOKEVIRTUAL,
										"javax/aejb/TransactionTrigger",
										"trigger",
										"(Ljava/lang/String;Ljava/lang/String;)V"));
								insns.insert(i1.getNext(), trig);
							} else {
								if (i1.getOpcode()>=IRETURN && i1.getOpcode()<=RETURN) {
									InsnList trig = new InsnList();
									trig.add(new VarInsnNode(ALOAD, localnum));
									trig.add(new VarInsnNode(ALOAD, 0));
									trig.add(new MethodInsnNode(INVOKEVIRTUAL,
											"java/lang/Object", "toString",
											"()Ljava/lang/String;"));
									trig.add(new MethodInsnNode(INVOKEVIRTUAL,
											"javax/aejb/TransactionTrigger",
											"trigger", "(Ljava/lang/String;)V"));
									insns.insert(i1.getPrevious(), trig);
								}
							}

						}

					}
					insns.insert(insns.getFirst(), begin);

				} else {
					InsnList insns = mn.instructions;
					Iterator<AbstractInsnNode> i = insns.iterator();
					while (i.hasNext()) {
						AbstractInsnNode i1 = i.next();
						if (i1 instanceof MethodInsnNode) {
							String owner = ((MethodInsnNode) i1).owner;
							if (ejbs.contains(owner)) {
								InsnList il = new InsnList();
								il.add(new TypeInsnNode(NEW,
										"java/lang/StringBuilder"));
								il.add(new InsnNode(DUP));
								il.add(new LdcInsnNode( "/"));
								il.add(new MethodInsnNode(INVOKESPECIAL,
										"java/lang/StringBuilder", "<init>",
										"(Ljava/lang/String;)V"));
								il.add(new VarInsnNode(ALOAD, 0));
								il.add(new MethodInsnNode(INVOKEVIRTUAL,
										"java/lang/Object", "toString",
										"()Ljava/lang/String;"));
								il.add(new MethodInsnNode(INVOKEVIRTUAL,
										"java/lang/StringBuilder", "append",
										"(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
								il.add(new MethodInsnNode(INVOKEVIRTUAL,
										"java/lang/StringBuilder", "toString",
										"()Ljava/lang/String;"));
								insns.insert(i1.getPrevious(), il);

								insns.insert(
										i1.getPrevious(),
										new MethodInsnNode(
												INVOKEVIRTUAL,
												((MethodInsnNode) i1).owner,
												((MethodInsnNode) i1).name,
												((MethodInsnNode) i1).desc
														.replace("(",
																"(Ljava/lang/String;")));
								insns.remove(i1);
							}

						}
					}
				}

			}
		}

	}

	//查询每个状态的next state及相应的触发事件
	public void setNext() {
		List<Event> event = stateMachine.getEvents();
		List state = stateMachine.getStates();
		for (int i = 0; i < stateMachine.getStatesCount(); i++) {
			String nexts = "";
			int sn = (Integer) state.get(i);
			int j;
			for (j = 0; j < event.size(); j++) {
				if (sn == event.get(j).getHead()) {
					nexts = nexts + event.get(j).getEvent() + "-"
							+ state.indexOf(event.get(j).getTail()) + ",";
				}
			}
			if (nexts.length() > 1) {
				nexts.subSequence(0, nexts.length() - 2);
			}
			next.add(nexts);
			System.out.println(i + "next:" + nexts);
		}
	}

	//判断该方法是否是需要分析的方法
	public boolean isTransaction(MethodNode mn, String annotationDesc) {

		if (mn.visibleAnnotations != null) {
			Iterator<AnnotationNode> i = mn.visibleAnnotations.iterator();
			while (i.hasNext()) {
				AnnotationNode an = i.next();
				if (annotationDesc.equals(an.desc)) {
					return true;
				}
			}
		}
		return false;
	}

	//查找外部依赖的EJB
	public void setEjbs(ClassNode cn) {

		if (cn.visibleAnnotations != null) {
			Iterator<AnnotationNode> i = cn.visibleAnnotations.iterator();
			while (i.hasNext()) {
				AnnotationNode an = i.next();
				// System.out.println(an.values);
				if (an.desc.contains("AEjb") || an.desc.contains("Ejb")) {
					String ejb = an.values.get(1).toString().split("/")[0];
					ejbs.add(ejb);
				}
			}
		}

		for (FieldNode fn : (List<FieldNode>) cn.fields) {
			if (fn.visibleAnnotations != null) {
				Iterator<AnnotationNode> fi = fn.visibleAnnotations.iterator();
				while (fi.hasNext()) {
					AnnotationNode fa = fi.next();
					if (fa.desc.contains("AEjb") || fa.desc.contains("Ejb")) {
						String ejb = fa.values.get(1).toString().split("/")[0];
						ejbs.add(ejb);
						// System.out.println(ejb);
					}
				}
			}
		}
	}

	//抽取程序字节码的control flow 
	public void ExtractControlFlow(String owner, final MethodNode mn)
			throws AnalyzerException {
		Analyzer a = new Analyzer(new BasicInterpreter()) {

			@Override
			protected void newControlFlowEdge(int src, int dst) {

				controlflow.addFlow(src, dst);
				if (src > dst) {
					controlflow.getFlow(src).setIsWhile(true);
				}
			}
		};
		a.analyze(owner, mn);
	}

	//递归实现抽取状态̬
	public void recognize_state(int src, int last_state, InsnList insns) {
		if (src < stateMachine.getEnd()) {
			if (stateMachine.getStates().contains(src)) {
				stateMachine.addEvent(new Event(last_state, src, ""));
			} else {
				AbstractInsnNode an = insns.get(src);
				if (an instanceof MethodInsnNode) {
					String owner = ((MethodInsnNode) an).owner;
					/*
					 * if(!future.contains(owner)){ future.add(owner); }
					 */
					String e = "Ejb." + owner + "."
							+ ((MethodInsnNode) an).name;
					if (ejbs.contains(owner)) {
						stateMachine.addState(src);
						stateMachine.addEvent(new Event(last_state, src, e));
						last_state = src;
						recognize_state(src + 1, last_state, insns);
					}
				} else {
					if (an instanceof JumpInsnNode) {

						int dst = -1;
						if (an.getOpcode() == GOTO) {
							dst = (Integer) controlflow.getFlow(src).dst.get(0);
							recognize_state(dst + 1, last_state, insns);
						} else {
							stateMachine.addState(src);
							stateMachine
									.addEvent(new Event(last_state, src, ""));
							if (controlflow.getFlow(src).isWhile) {
								for (int k = 0; k < controlflow.getDstSize(src); k++) {
									dst = (Integer) controlflow.getDst(src)
											.get(k);
									if (src < dst) {
										stateMachine.addState(dst);
										stateMachine.addEvent(new Event(src,
												dst, "while(" + src + ")F"));
										jumpinf.put(insns.get(dst), "while("
												+ src + ")F");
										recognize_state(dst + 1, dst, insns);
									} else {
										stateMachine.addState(dst);
										stateMachine.addEvent(new Event(src,
												dst, "while(" + src + ")T"));
										jumpinf.put(insns.get(dst), "while("
												+ src + ")T");
										recognize_state(dst + 1, dst, insns);
									}
								}
							} else {
								for (int k = 0; k < controlflow.getDstSize(src); k++) {
									dst = (Integer) controlflow.getDst(src)
											.get(k);
									stateMachine.addState(dst);
									stateMachine.addEvent(new Event(src, dst,
											"if(" + src + ")" + k));
									jumpinf.put(insns.get(dst), "if(" + src
											+ ")" + k);
									recognize_state(dst + 1, dst, insns);
								}
							}
						}
					} else {
						if(an.getOpcode()>=IRETURN && an.getOpcode()<=RETURN){
							stateMachine.addState(src);
							stateMachine.addEvent(new Event(last_state, src, "end"));
						}
						else{
						recognize_state(src + 1, last_state, insns);
						}
						}
				}
			}
		} else {
			stateMachine.addState(stateMachine.getEnd());
			stateMachine.addEvent(new Event(last_state, src, "end"));
		}
	}

    //合并状态	
	public void mergeState() {
		stateMachine.getStates().add(0, stateMachine.getStart());
		for (int i = 0; i < stateMachine.getEvents().size(); i++) {
			Event event = stateMachine.getEvents().get(i);
			if (event.getEvent().isEmpty() || event.getEvent().contains("end")) {
				int src = event.getHead();
				int dst = event.getTail();
				stateMachine.mergeStates(src, dst);
				stateMachine.getEvents().remove(i);
			}
		}
	}

	//显示所有状态变化
	public void showEvent() {
		for (int i = 0; i < stateMachine.getEvents().size(); i++) {
			Event event = stateMachine.getEvents().get(i);
			int src = event.getHead();
			int dst = event.getTail();
			System.out.println(src + "-" + event.getEvent() + "-" + dst);
			// System.out.println(event.getEvent().split(".")[0]);

		}
	}

	//抽取future和past
	public void ExtractMetaData() {
		int states_count = stateMachine.getStatesCount();
		
		List[] state = new LinkedList[states_count];
		for (int i = 0; i < states_count; i++)
			state[i] = new LinkedList();

		List s = stateMachine.getStates();
		// 输出所有的状态信息
		for (int i = 0; i < states_count; i++) {
			System.out.println(i + ":" + s.get(i));

		}
		// ///////////////////
		List<Event> e = stateMachine.getEvents();
		//两个数组分别存放每个状态的future和past
		past = new LinkedList[states_count];
		for (int i = 0; i < states_count; i++)
			past[i] = new LinkedList();
		future = new LinkedList[states_count];
		for (int i = 0; i < states_count; i++)
			future[i] = new LinkedList();

		//首先从前到后遍历事件列表，为每一个状态添加past，思路是：对于s0-a1-s1这一事件，s1的past=a0+s0的past
		boolean changed = true;
		while (changed) {
			changed = false;
			for (int i = 0; i < e.size(); i++) {
				Event event = (Event) e.get(i);
				int head = event.getHead();
				int tail = event.getTail();
				String port = event.getPort();

				int headindex = s.indexOf(head);
				int tailindex = s.indexOf(tail);
				if (port != null && !past[tailindex].contains(port)) {
					past[tailindex].add(port);
					changed = true;
				}
				if (head != stateMachine.getStart() && headindex != -1)
					for (int j = 0; j < past[headindex].size(); j++) {
						if (!past[tailindex].contains(past[headindex].get(j))) {
							past[tailindex].add(past[headindex].get(j));
							changed = true;
						}
					}
			}
		}
		//然后从后到前遍历事件列表，为每一个状态添加future，思路类似之前
		changed = true;
		while (changed) {
			changed = false;
			for (int i = e.size() - 1; i >= 0; i--) {
				Event event = (Event) e.get(i);
				int head = event.getHead();
				int tail = event.getTail();
				String port = event.getPort();
				int headindex = s.indexOf(head);
				int tailindex = s.indexOf(tail);
				if (port != null && !future[headindex].contains(port)) {
					future[headindex].add(port);
					changed = true;
				}
				if (tail != stateMachine.getEnd())
					for (int j = 0; j < future[tailindex].size(); j++) {
						if (!future[headindex].contains(future[tailindex]
								.get(j))) {
							future[headindex].add(future[tailindex].get(j));
							changed = true;
						}

					}
			}
		}
		for (int i = 0; i < states_count; i++) {
			int k = 0, p = 0;
			String sall = "";
			if (future[i].size() > 0) {
				for (k = 0; k < future[i].size() - 1; k++) {
					sall = sall + future[i].get(k) + ",";
				}
				sall = sall + future[i].get(k);
			}
			sall = sall + ";";
			if (past[i].size() > 0) {
				for (p = 0; p < past[i].size() - 1; p++) {
					sall = sall + past[i].get(p) + ",";
				}
				sall = sall + past[i].get(p);
			}
			System.out.println(i + ":" + sall);
			stateall.add(sall);
		}

	}

	//显示字节码文件
	public static void showClassSource(String file) {
		FileInputStream is;
		ClassReader cr;
		try {
			is = new FileInputStream(file);
			cr = new ClassReader(is);
			TraceClassVisitor trace = new TraceClassVisitor(new PrintWriter(
					System.out));
			cr.accept(trace, ClassReader.EXPAND_FRAMES);
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ClassVisitor getClassAdapter(final ClassVisitor cvr) {
		return new ClassNode() {
			@Override
			public void visitEnd() {
				transform(this);
				this.accept(cvr);
			}
		};
	}

	public static void main(String args[]) {
		try {

			FileInputStream in = new FileInputStream("C:\\Users\\SuPing\\workspace\\asmtest\\bin\\Ttest.class");
			//showClassSource("C:\\Users\\SuPing\\workspace\\asmtest\\bin\\test.class");
			ProgramAnalyzer t = new ProgramAnalyzer();
			ClassReader cr = new ClassReader(in);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			ClassVisitor cv = t.getClassAdapter(cw);
			cr.accept(cv, 0);
			byte[] b = cw.toByteArray();

			// 获得分析文件的文件名，并将新生成的文件存放入E:\analize_result下
			// String infilename=args[1].toString();
			String infilename = "C:\\Users\\SuPing\\workspace\\asmtest\\bin\\Ttest.class";
			StringTokenizer filename = new StringTokenizer(infilename, "\\");
			String ss[] = new String[filename.countTokens()];
			int i = 0;
			while (filename.hasMoreTokens()) {
				ss[i++] = filename.nextToken();
			}
			String file = ss[i - 1];
			System.out.println(file);
			File dir = new File("e:\\analize_result");
			dir.mkdir();
			File out = new File("e:\\analize_result\\" + file);

			FileOutputStream fout = new FileOutputStream(out);
			fout.write(b);
			fout.close();
			showClassSource("e:\\analize_result\\" + file);

		}

		catch (Exception e) {
			e.printStackTrace();

		}

	}

}

