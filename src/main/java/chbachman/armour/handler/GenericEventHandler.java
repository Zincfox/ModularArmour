package chbachman.armour.handler;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import chbachman.armour.items.ItemModularArmour;
import chbachman.armour.objects.PlayerArmourSlot;
import chbachman.armour.reference.ArmourSlot;
import chbachman.armour.upgrade.Upgrade;
import chbachman.armour.util.NBTHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;


public class GenericEventHandler{
	
	private Map<PlayerArmourSlot, ItemStack> playerList = new HashMap<PlayerArmourSlot,ItemStack>();
	
	@SubscribeEvent
	public void onEntityLivingUpdate(LivingUpdateEvent e){
		
	    //System.out.println(playerList);
	    
		if(e.entity instanceof EntityPlayer && e.entity.worldObj.isRemote == false){
			EntityPlayer player = (EntityPlayer) e.entity;
			
			ItemStack[] stacks = player.inventory.armorInventory;
			
			for(int i = 0; i < stacks.length; i++){
				
				//I have no idea why i - 1 needs to happen, or even works, but it does. 
				PlayerArmourSlot playerSlot = new PlayerArmourSlot(player, i - 1);
				
				ItemStack stack = stacks[i];
				
				if (stack != null && stack.getItem() instanceof ItemModularArmour) {

					if (!playerList.containsKey(playerSlot)) {
						playerList.put(playerSlot, stack);
					}

				} else {

					if (playerList.containsKey(playerSlot)) {

						ItemStack stack2 = playerList.get(playerSlot);
						
						ItemModularArmour armour = (ItemModularArmour) stack2.getItem();
						
						if (playerSlot.getSlot() == armour.getArmourSlot()) {
							armour.onArmorTakeOff(player.worldObj, player, stack2);

							playerList.remove(playerSlot);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent e){
		for(ItemStack stack : e.player.inventory.armorInventory){
			if(stack != null && stack.getItem() instanceof ItemModularArmour){
				
				ItemModularArmour armour = (ItemModularArmour) stack.getItem();
				
				for(Upgrade upgrade : NBTHelper.getUpgradeListFromNBT(stack.stackTagCompound)){
					upgrade.onArmourEquip(e.player.worldObj, e.player, stack, ArmourSlot.getArmourSlot(armour.armorType));
				}
				
				
			}
		}
	}
}
