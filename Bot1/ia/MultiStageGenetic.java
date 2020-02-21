package ia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ai.core.AI;
import rts.GameState;
import rts.units.UnitTypeTable;
import scripts.*;
import scripts.BarrackBehavior.BarBehType;
import scripts.BaseBehavior.BaseBehType;
import scripts.HeavyBehavior.HeavyBehType;
import scripts.LightBehavior.LightBehType;
import scripts.RangedBehavior.RangedBehType;
import scripts.WorkerBehavior.WorkBehType;

public class MultiStageGenetic {
	
	private final int TOURNSIZE = 10;
	private final double MUT_CHANCE = 0.1;
	private final int MAX_CYCLES = 2000;
	
	private GameState gs;
	private UnitTypeTable utt;
	private List<AI> population;
	private List<AI> bestPopulation;
	private double[] evaluation;
	private int popSize; 
	private int bestSize;
	private int eliteSize;
	private boolean visual;
	private int phases;
	
	
	public MultiStageGenetic(int a_popSize, int a_bestSize, int a_eliteSize, int a_phases, UnitTypeTable a_utt, 
					GameState a_gs,  boolean a_visual) {
		gs = a_gs;
		utt = a_utt;
		popSize = a_popSize;
		bestSize = a_bestSize;
		eliteSize = a_eliteSize;
		phases = a_phases;
		evaluation = new double[popSize];
		visual = a_visual;					
		population = new ArrayList<AI>();
		bestPopulation = new ArrayList<AI>();

	}
	
	public void getInitialPopulation() {
		Random r = new Random();
		for (int i = 0; i < popSize; ++i) {
			List<GeneralScript> scripts = new ArrayList<GeneralScript>();
			for (int j = 0; j < phases; ++j) {
				BaseBehType baseBehType = BaseBehType.values()[r.nextInt(BaseBehType.values().length)];
				BarBehType barBehType = BarBehType.values()[r.nextInt(BarBehType.values().length)];
				WorkBehType workBehType = WorkBehType.values()[r.nextInt(WorkBehType.values().length)];
				LightBehType lightBehType = LightBehType.values()[r.nextInt(LightBehType.values().length)];
				HeavyBehType heavyBehType = HeavyBehType.values()[r.nextInt(HeavyBehType.values().length)];
				RangedBehType rangedBehType = RangedBehType.values()[r.nextInt(RangedBehType.values().length)];
				scripts.add(new GeneralScript(utt, baseBehType, barBehType, 
						workBehType, lightBehType, heavyBehType, rangedBehType));
			}
			population.add(new MultiStageGeneralScript(scripts));
		}
	}
	
	public void select(ArrayList<MultiStageGeneralScript> newPopulation) {
		Random r = new Random();
		for (int i = 0; i < popSize - eliteSize; ++i) {
			int best = -1; double bestEval = -100000;
			for (int j = 0; j < TOURNSIZE; ++j) {
				int a = r.nextInt(popSize);
				if (bestEval < evaluation[a]) {
					bestEval = evaluation[a];
					best = a;
				}
			}
			newPopulation.add((MultiStageGeneralScript) population.get(best));
		}
	}
	
	public void cross(ArrayList<MultiStageGeneralScript> newPopulation) {
		Random r = new Random();
		ArrayList<MultiStageGeneralScript> crossPopulation = new ArrayList<MultiStageGeneralScript>();
		while (crossPopulation.size() < newPopulation.size()) {
			int p1 = r.nextInt(newPopulation.size());
			int p2 = r.nextInt(newPopulation.size());
			int x = r.nextInt(6);
			int y = r.nextInt(phases);
			
			List<GeneralScript> scripts1 = newPopulation.get(p1).getScripts();
			List<GeneralScript> scripts2 = newPopulation.get(p2).getScripts();
			
			List<String> param1 = scripts1.get(y).getBehaviorTypes();
			List<String> param2 = scripts2.get(y).getBehaviorTypes();

			List<String> nparam1 = new ArrayList<String>();
			List<String> nparam2 = new ArrayList<String>();

			for (int i = y + 1; i < phases; ++i) {
				GeneralScript aux = scripts1.get(i);
				scripts1.set(i, scripts1.get(i));
				scripts2.set(i, aux);
			}
			
			for (int i = 0; i < 6; ++i) {
				if (i < x) {
					nparam1.add(param1.get(i));
					nparam2.add(param2.get(i));
				} else {
					nparam1.add(param2.get(i));
					nparam2.add(param1.get(i));
				}
			}
			
			scripts1.set(y, new GeneralScript(scripts1.get(y).getUtt(),
					newPopulation.get(p1).getTimeBudget(), newPopulation.get(p1).getIterationsBudget(),
					BaseBehType.valueOf(nparam1.get(0)), BarBehType.valueOf(nparam1.get(1)), 
					WorkBehType.valueOf(nparam1.get(2)), LightBehType.valueOf(nparam1.get(3)),
					HeavyBehType.valueOf(nparam1.get(4)), RangedBehType.valueOf(nparam1.get(5))));
			scripts2.set(y, new GeneralScript(scripts2.get(y).getUtt(),
					newPopulation.get(p1).getTimeBudget(), newPopulation.get(p1).getIterationsBudget(),
					BaseBehType.valueOf(nparam2.get(0)), BarBehType.valueOf(nparam2.get(1)), 
					WorkBehType.valueOf(nparam2.get(2)), LightBehType.valueOf(nparam2.get(3)),
					HeavyBehType.valueOf(nparam2.get(4)), RangedBehType.valueOf(nparam2.get(5))));
			
			crossPopulation.add(new MultiStageGeneralScript(scripts1));		
			crossPopulation.add(new MultiStageGeneralScript(scripts2));	
		}
		newPopulation = crossPopulation;
	}
	
	public void mutate (ArrayList<MultiStageGeneralScript> newPopulation) {
		Random r = new Random();
		for (int i = 0; i < newPopulation.size(); ++i) {
			List<GeneralScript> scripts = newPopulation.get(i).getScripts();
			for (int j = 0; j < scripts.size(); ++j) {
				List<String> param = scripts.get(j).getBehaviorTypes();
				for (int k = 0; k < param.size(); ++k) {
					if (r.nextDouble() < MUT_CHANCE) {
						switch(k) {
						case 0 : param.set(k, BaseBehType.values()[r.nextInt(BaseBehType.values().length)].toString()); break;
						case 1 : param.set(k, BarBehType.values()[r.nextInt(BarBehType.values().length)].toString()); break;
						case 2 : param.set(k, WorkBehType.values()[r.nextInt(WorkBehType.values().length)].toString()); break;
						case 3 : param.set(k, LightBehType.values()[r.nextInt(LightBehType.values().length)].toString()); break;
						case 4 : param.set(k, HeavyBehType.values()[r.nextInt(HeavyBehType.values().length)].toString()); break;
						case 5 : param.set(k, RangedBehType.values()[r.nextInt(RangedBehType.values().length)].toString()); break;
						}
					}
				}
				scripts.set(j,new GeneralScript(scripts.get(j).getUtt(),
						newPopulation.get(j).getTimeBudget(), newPopulation.get(j).getIterationsBudget(),
						BaseBehType.valueOf(param.get(0)), BarBehType.valueOf(param.get(1)), 
						WorkBehType.valueOf(param.get(2)), LightBehType.valueOf(param.get(3)),
						HeavyBehType.valueOf(param.get(4)), RangedBehType.valueOf(param.get(5))));
			}
			newPopulation.set(i, new MultiStageGeneralScript(scripts));
		}
	}
	
	public void elite(ArrayList<MultiStageGeneralScript> newPopulation) {
		double[] evalCopy = evaluation.clone();
		double bestEval = -100000;
		int best = -1;
		for (int i = 0; i < eliteSize; ++i) {
			for (int j = 0; j < evalCopy.length; ++j) {
				if (evalCopy[j] > bestEval) {
					bestEval = evalCopy[j];
					best = j;
				}
			}
			newPopulation.add((MultiStageGeneralScript) population.get(best));
			evalCopy[best] = bestEval = -100000;
			best = -1;
		}
	}
	
	public void evolutionaryAlgorithm(int maxGen) {
		int k = 0;
		population = new ArrayList<AI>(); 
		getInitialPopulation();
		while (k < maxGen) {
			ArrayList<MultiStageGeneralScript> newPopulation = new ArrayList<MultiStageGeneralScript>();
			try {
				List<AI> popAux = new LinkedList<>();
				for (AI bot : population)
					popAux.add(bot.clone());
				evaluation = ThreadedTournament.evaluate(population, popAux, Arrays.asList(gs.getPhysicalGameState()), utt, 1,
						MAX_CYCLES, 100, visual, System.out, -1, false, false, "traces/");
			} catch (Exception e) {
				e.printStackTrace();
			}
			select(newPopulation);
			cross(newPopulation);
			mutate(newPopulation);
			elite(newPopulation);
			++k;
			
			System.out.println("Generación " + k + " de " + maxGen);
		} 
		try {
			List<AI> popAux = new LinkedList<>();
			for (AI bot : population)
				popAux.add(bot.clone());
			evaluation = ThreadedTournament.evaluate(population, popAux, Arrays.asList(gs.getPhysicalGameState()), utt, 1,
					MAX_CYCLES, 100, visual, System.out, -1, false, false, "traces/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		double[] evalCopy = evaluation.clone();
		bestPopulation = new ArrayList<AI>();
		double bestEval = -100000;
		int best = -1;
		for (int i = 0; i < bestSize; ++i) {
			for (int j = 0; j < evalCopy.length; ++j) {
				if (evalCopy[j] > bestEval) {
					bestEval = evalCopy[j];
					best = j;
				}
			}
			bestPopulation.add((GeneralScript) population.get(best));
			evalCopy[best] = bestEval = -100000;
			best = -1;
		}
	}
	
	public List<AI> getBestPopulation() {
		List<AI> aux = new LinkedList<>();
		for (AI bot : bestPopulation)
			aux.add(bot.clone());
		return aux;
	}
}
