package chbachman.armour.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import chbachman.armour.ModularArmour;
import chbachman.armour.gui.GuiHandler;
import chbachman.armour.reference.ArmourSlot;
import chbachman.armour.upgrade.Upgrade;
import chbachman.armour.util.NBTHelper;
import cofh.api.item.IInventoryContainerItem;
import cofh.util.CoreUtils;
import cofh.util.StringHelper;

public class ItemModularArmour extends ItemSpecialArmour implements IInventoryContainerItem{
	
	protected int ratio = 0;
	protected int max = 0;
	
	public ItemModularArmour(ArmorMaterial material, int type) {
		super(material,type);
		this.setCreativeTab(CreativeTabs.tabCombat);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean check) {
	    NBTHelper.createDefaultStackTag(stack);
		if (!StringHelper.isShiftKeyDown()) {
			list.add(StringHelper.shiftForDetails());
		}else{
		    
	        list.add(StringHelper.localize("info.cofh.charge") + ": " + stack.stackTagCompound.getInteger("Energy") + " / " + capacity.get(stack) + " RF");
		}
		
		if(!StringHelper.isControlKeyDown() && NBTHelper.getNBTTagList(stack.stackTagCompound).tagCount() != 0){
		    list.add(StringHelper.LIGHT_GRAY + StringHelper.localize("info.cofh.hold") + " " + StringHelper.YELLOW + StringHelper.ITALIC + StringHelper.localize("info.chbachman.control") + " " + StringHelper.END + StringHelper.LIGHT_GRAY
                + StringHelper.localize("info.chbachman.upgradeList") + StringHelper.END);
		}else if(NBTHelper.getNBTTagList(stack.stackTagCompound).tagCount() != 0){
		    for(Upgrade upgrade : NBTHelper.getUpgradeListFromNBT(stack.stackTagCompound)){
		        list.add(upgrade.getName());
		    }
		}
	}
	
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity player, int x, boolean y){
	    stack.stackTagCompound.setBoolean("HasPutOn", false);
	}
	
	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack){
	    
		NBTHelper.createDefaultStackTag(stack);
		
		NBTTagList list = NBTHelper.getNBTTagList(stack.stackTagCompound);
		
		int energy = 0;
		
		for(int i = 0; i < list.tagCount(); i++){
			Upgrade upgrade = Upgrade.getUpgradeFromNBT(list.getCompoundTagAt(i));
			
			if (upgrade != null) {
                upgrade.onArmourTick(world, player, stack, ArmourSlot.getArmourSlot(this.armorType));
                
                energy += upgrade.getEnergyTick(world, player, stack, ArmourSlot.getArmourSlot(this.armorType));
            }

		}
		
		this.extractEnergy(stack, energy, false);
		
		
		if(!stack.stackTagCompound.getBoolean("HasPutOn")){
			stack.stackTagCompound.setBoolean("HasPutOn", true);

			for(int i = 0; i < list.tagCount(); i++){
				Upgrade upgrade = Upgrade.getUpgradeFromNBT(list.getCompoundTagAt(i));
				
				if (upgrade != null) {
                    upgrade.onArmourEquip(world, player, stack, ArmourSlot.getArmourSlot(this.armorType));
                }
			}
		}
		
		if(stack.stackTagCompound.getInteger("Energy") == 0){
			for(int i = 0; i < list.tagCount(); i++){
				Upgrade upgrade = Upgrade.getUpgradeFromNBT(list.getCompoundTagAt(i));
				
				if (upgrade != null) {
				    upgrade.onNoEnergy(world, player, stack, ArmourSlot.getArmourSlot(this.armorType));
				}
			}
		}
		
	}
	
	public void onArmorTakeOff(World world, EntityPlayer player, ItemStack stack){
		
		if(stack == null){
			return;
		}
		
		NBTHelper.createDefaultStackTag(stack);
		
		stack.stackTagCompound.setBoolean("HasPutOn", false);
		
		NBTTagList list = NBTHelper.getNBTTagList(stack.stackTagCompound);
		
		for(int i = 0; i < list.tagCount(); i++){
			Upgrade upgrade = Upgrade.getUpgradeFromNBT(list.getCompoundTagAt(i));
			
			upgrade.onArmourDequip(world, player, stack, ArmourSlot.getArmourSlot(this.armorType));
		}
	}
	
	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
		int sum = 0;
		
		NBTTagList nbt = NBTHelper.getNBTTagList(armor.stackTagCompound);
		
		for(int i = 0; i < nbt.tagCount(); i++){
			sum += Upgrade.getUpgradeFromNBT(nbt.getCompoundTagAt(i)).getArmourDisplay();
		}
		return sum;
	}
	
	@Override
    public ItemStack onItemRightClick (ItemStack stack, World world, EntityPlayer player)
    {
		
		if(CoreUtils.isFakePlayer(player)){
			return stack;
		}
		
		if(player.isSneaking()){
			
			this.receiveEnergy(stack, 1000, false);
			return stack;
			//return super.onItemRightClick(stack, world, player);
		}
		
		if(world.isRemote == false){
			player.openGui(ModularArmour.instance, GuiHandler.ARMOUR_ID, world, 0, 0, 0);
		}
        return stack;
    }
	
	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
		if(source.isUnblockable()){
			return USELESS;
		}

		double ratio = 0;

		NBTTagList nbt = NBTHelper.getNBTTagList(armor.stackTagCompound);

		for(int i = 0; i < nbt.tagCount(); i++){
			ratio += Upgrade.getUpgradeFromNBT(nbt.getCompoundTagAt(i)).getDefenceRatio(slot);
		}

		return new ArmorProperties(0, ratio, Integer.MAX_VALUE);
	}
	
	//IInventoryContainerItem
	@Override
	public int getSizeInventory(ItemStack container) {
		return 9;
	}

}
