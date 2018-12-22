package ferro2000.immersivetech.common.blocks.metal.tileentities;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import ferro2000.immersivetech.api.ITUtils;
import ferro2000.immersivetech.api.client.MechanicalEnergyAnimation;
import ferro2000.immersivetech.api.energy.MechanicalEnergy;
import ferro2000.immersivetech.common.Config.ITConfig;
import ferro2000.immersivetech.common.blocks.ITBlockInterface.IMechanicalEnergy;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockAlternator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class TileEntityAlternator extends TileEntityMultiblockPart<TileEntityAlternator> implements IMechanicalEnergy, IAdvancedSelectionBounds,  IAdvancedCollisionBounds, IFluxProvider{

	FluxStorage energyStorage = new FluxStorage(ITConfig.Machines.alternator_energyStorage);
	public MechanicalEnergy mechanicalEnergy = new MechanicalEnergy();
	MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();
	
	private static int[] size = new int[] {3,4,3};
		
	public TileEntityAlternator() {
		super(size);
	}

	@Override
	public void update() {
		
		if(!world.isRemote && formed && pos==13) {
			
			if(ITUtils.checkMechanicalEnergyTransmitter(world, getPos())) {
				
				mechanicalEnergy = ITUtils.getMechanicalEnergy(world, getPos());
				
			}
			
			if(mechanicalEnergy.getEnergy()>0) {
				this.energyStorage.modifyEnergyStored(mechanicalEnergy.getEnergy() / ITConfig.Machines.alternator_RfModifier);
			}
			
			if(energyStorage.getEnergyStored()>0) {
				
				TileEntity tileEntity;
				EnumFacing f;
				int h = 0;
				
				for(int i=0;i<6;i++){
					
					f = facing;
					
					if(i<3) {
						
						f = f.rotateYCCW();
						h = i-1;
						
					}else {
						
						f = f.rotateY();
						h = i-4;
						
					}
					
					tileEntity = Utils.getExistingTileEntity(world, getPos().add(0, h, 0).offset(f, 2));
					EnumFacing energyFacing = f.getOpposite();
					
					if(EnergyHelper.isFluxReceiver(tileEntity, energyFacing)) {
						
						int output = ITConfig.Machines.alternator_RfPerTick;
						
						if(EnergyHelper.insertFlux(tileEntity, energyFacing, output, true)>0 && (this.energyStorage.getEnergyStored()>output)) {
							
							EnergyHelper.insertFlux(tileEntity, energyFacing, output, false);
							energyStorage.setEnergy(energyStorage.getEnergyStored()-output);
							
						}
						
					}
					
				}
				
			}
			
		}
		
	}
	
	public boolean canRunMechanicalEnergy() {
		
		if(energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored()) {
			
			return true;
			
		}
		
		return false;
		
	}
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		energyStorage.readFromNBT(nbt);
		mechanicalEnergy.readFromNBT(nbt);
		animation.readFromNBT(nbt);
		
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		energyStorage.writeToNBT(nbt);
		mechanicalEnergy.writeToNBT(nbt);
		animation.writeToNBT(nbt);
		
	}

	@Override
	public float[] getBlockBounds() {
		return null;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return false;
	}

	@Override
	public ItemStack getOriginalBlock() {
		if(pos<0)
			return ItemStack.EMPTY;
		ItemStack s = ItemStack.EMPTY;
		try{
			s = MultiblockAlternator.instance.getStructureManual()[pos/12][pos%12/3][pos%3];
		}catch(Exception e){e.printStackTrace();}
		return s.copy();
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds() {
		return getAdvancedSelectionBounds();
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds() {
		
double[] boundingArray = new double[6];
		
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		
		if(pos==0 || pos==2 || pos==12 || pos==14 || pos==24 || pos==26) {
			
			if(pos==2 || pos==14 || pos==26) {
				fw = fw.getOpposite();
			}
			
			
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, 0, .875f, .25f, .75f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, .625f, .25f, .25f, .75f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .75f, 0, .375f, .625f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			if(pos<=2) {
				
				boundingArray = ITUtils.smartBoundingBox(0, 0, .125f, .375f, 0, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
			}else {
				
				boundingArray = ITUtils.smartBoundingBox(0, 0, .125f, .375f, 0, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
								
			}
			
			return list;
			
		}
		
		if(pos==1 || pos==25) {
			
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, .25f, .25f, .25f, .75f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, 0, .75f, .375f, .625f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .75f, 0, .375f, .625f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			if(pos==1) {
				
				boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .375f, .375f, .75f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
												
			}else {
				
				boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .375f, .375f, 0, .25f, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
								
			}
			
			return list;
			
		}
		
		if(pos==3 || pos==5) {
			
			if(pos==5) {
				fw = fw.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .5f, 0, 0, 1, fl, fw);
					
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
		
		if(pos==9 || pos==11) {
			
			if(pos==11) {
				fw = fw.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(.5f, 0, 0, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .5f, 0, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==13) {
			
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, .125f, .125f, .125f, .875f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, 0, .875f, .375f, .625f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .875f, 0, .375f, .625f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .375f, .375f, .875f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .375f, .375f, 0, .125f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .25f, .25f, .25f, .75f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
						
		}
		
		if(pos==15 || pos==17) {
			
			if(pos==17) {
				fw = fw.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .75f, 0, 0, .75f, fl, fw);
			
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
		}
		
		if(pos==16) {
			
			boundingArray = ITUtils.smartBoundingBox(.5f, 0, 0, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.25f, .5f, .25f, .25f, .25f, .75f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, 0, 0, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==18 || pos==20) {
			
			if(pos==20) {
				fw = fw.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(0, .25f, .25f, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, .75f, 0, 0, .75f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
					
			return list;
			
		}
		
		if(pos==21 || pos==23) {
			
			if(pos==23) {
				fw = fw.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, .25f, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, .75f, 0, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
					
			return list;
			
		}
		
		if(pos==30 || pos==32) {
			
			if(pos==32) {
				fw = fw.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(0, .25f, .5f, 0, 0, .4375f, fl, fw);
			
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
		
		if(pos==31) {
			
			boundingArray = ITUtils.smartBoundingBox(0, .25f, 0, 0, 0, .5f, fl, fw);
			
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
		}
		
		if(pos==33 || pos==35) {
			
			if(pos==35) {
				fw = fw.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, .5f, 0, 0, .4375f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, .75f, 0, 0, .125f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==34) {
			
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, 0, 0, 0, .5f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, 0, 0, 0, .125f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
						
		}
		
		return null;
		
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop,
			ArrayList<AxisAlignedBB> list) {
		return false;
	}

	@Override
	public boolean canConnectEnergy(@Nullable EnumFacing from) {
		return pos==0 || pos==2 || pos==12 || pos==14 || pos==24 || pos==26;
	}

	@Override
	public int extractEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
		if(pos!=0 || pos!=2 || pos!=12 || pos!=14 || pos!=24 || pos!=26)
			return 0;
		TileEntityAlternator master = master();
		return master==null?0:master.energyStorage.extractEnergy(energy, simulate);
	}

	@Override
	public int getEnergyStored(@Nullable EnumFacing from) {
		TileEntityAlternator master = master();
		return master==null?0:master.energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(@Nullable EnumFacing from) {
		TileEntityAlternator master = master();
		return master==null?0:master.energyStorage.getMaxEnergyStored();
	}

	@Override
	public boolean isMechanicalEnergyTransmitter() {
		return false;
	}

	@Override
	public boolean isMechanicalEnergyReceiver() {
		return true;
	}

	@Override
	public EnumFacing getMechanicalEnergyOutputFacing() {
		return null;
	}

	@Override
	public EnumFacing getMechanicalEnergyInputFacing() {
		return facing;
	}
	
	@Override
	public int inputToCenterDistance() {
		return 3;
	}

	@Override
	public int outputToCenterDistance() {
		return -1;
	}

	@Override
	public MechanicalEnergy getEnergy() {
		return mechanicalEnergy;
	}
	
	public MechanicalEnergyAnimation getAnimation() {
		return animation;
	}
	
}
