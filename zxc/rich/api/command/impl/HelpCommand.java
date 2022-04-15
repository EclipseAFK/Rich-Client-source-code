package zxc.rich.api.command.impl;

import com.mojang.realmsclient.gui.ChatFormatting;

import zxc.rich.Main;
import zxc.rich.api.command.CommandAbstract;

public class HelpCommand extends CommandAbstract {

    public HelpCommand() {
        super("help", "help", ".help", "help");
    }

    @Override
    public void execute(String... args) {
        if (args.length == 1) {
            if (args[0].equals("help")) {
                Main.msg(ChatFormatting.AQUA + "All Commands:", true);
                Main.msg(ChatFormatting.WHITE + ".bind -> (��������� ��������� �������)", true);
                Main.msg(ChatFormatting.WHITE + ".macro -> (��������� ��������� ������� �� ������� ������)", true);
                Main.msg(ChatFormatting.WHITE + ".panic -> (��������� ��������� ��� ������ ����)", true);
                Main.msg(ChatFormatting.WHITE + ".vclip -> (��������� ����������������� �� ���������)", true);
                Main.msg(ChatFormatting.WHITE + ".hclip -> (��������� ����������������� �� �����������)", true);

            }
        } else {
            Main.msg(this.getUsage(), true);
        }
    }
}
