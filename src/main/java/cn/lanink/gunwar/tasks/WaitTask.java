package cn.lanink.gunwar.tasks;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.PlayerGameData;
import cn.lanink.gunwar.room.base.RoomConfig;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;
import java.util.Map;


public class WaitTask extends PluginTask<GunWar> {

    private final Language language;
    private final BaseRoom room;

    public WaitTask(GunWar owner, BaseRoom room) {
        super(owner);
        this.language = owner.getLanguage();
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != IRoomStatus.ROOM_STATUS_WAIT) {
            this.cancel();
            return;
        }

        for (Player player : this.room.getPlayerDataMap().keySet()) {
            if (this.room.getSupplyType() != RoomConfig.SupplyType.CLOSE) {
                player.getInventory().setItem(0, Tools.getItem(13)); //商店
            }
            player.getInventory().setItem(3, Tools.getItem(11)); //队伍选择
            player.getInventory().setItem(5, Tools.getItem(12)); //队伍选择
            player.getInventory().setItem(8, Tools.getItem(10)); //退出房间
        }
        boolean isEvenPlayerNum = room.isEvenPlayerNum();
        if (this.room.getPlayerDataMap().size() >= this.room.getMinPlayers()
                && (isEvenPlayerNum && this.room.getPlayerDataMap().size() % 2 == 0) || !isEvenPlayerNum) {
            if (this.room.getPlayerDataMap().size() == this.room.getMaxPlayers() && this.room.waitTime > 10) {
                this.room.waitTime = 10;
            }
            this.room.waitTime--;

            if (this.room.waitTime > 0) {
                String title = "§e";
                if (this.room.waitTime <= 10) {
                    if (this.room.waitTime <= 3) {
                        title = "§c";
                        Tools.playSound(this.room, Sound.NOTE_HARP);
                    } else {
                        Tools.playSound(this.room, Sound.NOTE_BASSATTACK);
                    }
                    title += this.room.waitTime;
                    for (Player player : this.room.getPlayerDataMap().keySet()) {
                        player.sendTitle(title, "", 0, 15, 5);
                    }
                }
                for (Map.Entry<Player, PlayerGameData> entry : room.getPlayerDataMap().entrySet()) {
                    LinkedList<String> ms = new LinkedList<>();
                    for (String string : this.language.translateString("waitTimeScoreBoard").split("\n")) {
                        ms.add(string.replace("%gameMode%", Tools.getShowGameMode(this.room.getGameMode()))
                                .replace("%team%", entry.getValue().getTeam().getShowName())
                                .replace("%playerNumber%", room.getPlayerDataMap().size() + "")
                                .replace("%minPlayerNumber%", room.getMinPlayers() + "")
                                .replace("%maxPlayerNumber%", room.getMaxPlayers() + "")
                                .replace("%time%", room.waitTime + ""));
                    }
                    owner.getScoreboard().showScoreboard(entry.getKey(), this.language.translateString("scoreBoardTitle"), ms);
                }
            } else {
                this.room.startGame();
                Server.getInstance().getScheduler().scheduleDelayedTask(this.owner,
                        () -> Tools.playSound(this.room, Sound.NOTE_FLUTE), 2, true);
                this.cancel();
            }
        } else if (!this.room.getPlayerDataMap().isEmpty()) {
            if (this.room.waitTime != this.room.getSetWaitTime()) {
                this.room.waitTime = this.room.getSetWaitTime();
            }
            for (Map.Entry<Player, PlayerGameData> entry : room.getPlayerDataMap().entrySet()) {
                LinkedList<String> ms = new LinkedList<>();
                for (String string : language.translateString("waitScoreBoard").split("\n")) {
                    ms.add(string.replace("%gameMode%", Tools.getShowGameMode(this.room.getGameMode()))
                            .replace("%team%", entry.getValue().getTeam().getShowName())
                            .replace("%playerNumber%", room.getPlayerDataMap().size() + "")
                            .replace("%minPlayerNumber%", room.getMinPlayers() + "")
                            .replace("%maxPlayerNumber%", room.getMaxPlayers() + ""));
                }
                owner.getScoreboard().showScoreboard(entry.getKey(), this.language.translateString("scoreBoardTitle"), ms);
            }
        } else {
            this.room.endGame();
            this.cancel();
        }
    }

}
