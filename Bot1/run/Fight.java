package run;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ia.ThreadedTournament;
import ia.Tournament;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;
import scripts.GeneralScript;
import scripts.BarrackBehavior.BarBehType;
import scripts.BaseBehavior.BaseBehType;
import scripts.HeavyBehavior.HeavyBehType;
import scripts.LightBehavior.LightBehType;
import scripts.RangedBehavior.RangedBehType;
import scripts.WorkerBehavior.WorkBehType;

public class Fight {
	public static void main(String[] args) {

		UnitTypeTable utt = new UnitTypeTable();
		GameState gs = null;
		List<AI> bots = new ArrayList<AI>();
		bots.add(new GeneralScript(utt, BaseBehType.RUSHWORKER, BarBehType.RANGED,
				WorkBehType.HARVESTER, LightBehType.LESSHP, HeavyBehType.CLOSEST, RangedBehType.CLOSEST));
		bots.add(new GeneralScript(utt, BaseBehType.RUSHWORKER, BarBehType.LIGHT,
				WorkBehType.HARVESTER, LightBehType.CLOSEST, HeavyBehType.LESSPERCHP, RangedBehType.CLOSESTDIRECT));
		try {
			gs = new GameState(PhysicalGameState.load("maps/24x24/basesWorkers24x24.xml", utt), utt);
			Tournament.evaluate(bots, Arrays.asList(gs.getPhysicalGameState()), utt, 1, 3000, 300, true, System.out, -1,
					true, false, "traces/");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
