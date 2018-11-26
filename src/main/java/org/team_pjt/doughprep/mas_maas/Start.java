package org.team_pjt.doughprep.mas_maas;

import java.util.List;
import java.util.Vector;

public class Start {
    public static void main(String[] args) {
        JSONConverter.test_parsing();

        List<String> agents = new Vector<>();
        agents.add("BakingInterface:org.team_pjt.doughprep.mas_maas.agents.BakingInterface");
        agents.add("DoughManager:org.team_pjt.doughprep.mas_maas.agents.DoughManager");
        agents.add("KneadingMachineAgent:org.team_pjt.doughprep.mas_maas.agents.KneadingMachineAgent");
        agents.add("PreparationTableAgent:org.team_pjt.doughprep.mas_maas.agents.PreparationTableAgent");
        agents.add("Proofer:org.team_pjt.doughprep.mas_maas.agents.Proofer");
        agents.add("TimeKeeper:org.team_pjt.doughprep.mas_maas.agents.TimeKeeper");

        List<String> cmd = new Vector<>();
        cmd.add("-agents");
        StringBuilder sb = new StringBuilder();
        for (String a : agents) {
            sb.append(a);
            sb.append(";");
        }
        cmd.add(sb.toString());
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));
    }
}
