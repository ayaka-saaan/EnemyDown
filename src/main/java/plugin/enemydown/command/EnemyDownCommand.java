package plugin.enemydown.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import plugin.enemydown.Main;
import plugin.enemydown.data.PlayerScore;

public class EnemyDownCommand extends BaseCommand implements Listener {

  private Main main;
  private List<PlayerScore> playerScoreList = new ArrayList<>();

  public EnemyDownCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player){
      return onExecutePlayerCommand(player);
    } else {
      return onExecuteNPCCommand(sender);
    }
  }

  @Override
  public boolean onExecutePlayerCommand(Player player) {
    PlayerScore nowPlayer = getPlayerScore(player);
    nowPlayer.setGameTime(20);

    World world = player.getWorld();

    initPlayerStatus(player);

    Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
      if (nowPlayer.getGameTime() <= 0) {
        Runnable.cancel();
        player.sendTitle("FINISH!!!!!",
          nowPlayer.getPlayerName() + " " + nowPlayer.getScore(),
          5, 60, 5);
        player.sendMessage("FINISH!!!!!");
        nowPlayer.setScore(0);
        List<Entity> nearbyEnemies = player.getNearbyEntities(50, 0, 50);
          for (Entity enemy : nearbyEnemies) {
            switch (enemy.getType()) {
              case ZOMBIE, SKELETON, WITCH -> enemy.remove();
            }
          }
        return;
      }
      world.spawnEntity(getEnemySpawnLocation(player, world), getEnemy());
      nowPlayer.setGameTime(nowPlayer.getGameTime() - 5);
      }, 0, 5 * 20);

    return true;
  }


  @Override
  public boolean onExecuteNPCCommand(CommandSender sender) {
    return false;
  }

  private PlayerScore getPlayerScore(Player player) {
    if (playerScoreList.isEmpty()){
      return addNewPlayer(player);
    } else {
      for (PlayerScore playerScore : playerScoreList) {
        if (!playerScore.getPlayerName().equals(player.getName())) {
          return addNewPlayer(player);
        } else {
          return playerScore;
        }
      }
    }
    return null;
  }

  private PlayerScore addNewPlayer(Player player) {
    PlayerScore newPlayer = new PlayerScore();
    newPlayer.setPlayerName(player.getName());
    playerScoreList.add(newPlayer);
    return newPlayer;
  }

  @EventHandler
  public void onEnemyDeath(EntityDeathEvent e){
    LivingEntity enemy = e.getEntity();
    Player player = enemy.getKiller();
    if (Objects.isNull(player) || playerScoreList.isEmpty()) {
      return;
    }

    for (PlayerScore playerScore : playerScoreList) {
      if(playerScore.getPlayerName().equals(player.getName())){
        int point = switch (enemy.getType()) {
          case ZOMBIE -> 10;
          case SKELETON -> 20;
          case WITCH -> 30;
          default -> 0;
        };
        playerScore.setScore(playerScore.getScore() + point);
        player.sendMessage("Enemy Down!!! The current score is " + playerScore.getScore() + "!!");
      }
    }
  }
  private void initPlayerStatus (Player player) {
    player.setHealth(20);
    player.setFoodLevel(20);

    PlayerInventory inventory = player.getInventory();
    inventory.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
    inventory.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
    inventory.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
    inventory.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
    inventory.setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
  }

}
