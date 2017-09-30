package ferro2000.immersivetech.api;

import ferro2000.immersivetech.api.energy.MechanicalEnergy;
import ferro2000.immersivetech.common.blocks.ITBlockInterface.IMechanicalEnergy;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ITUtils {
	
	public static double[] smartBoundingBox(double A, double B, double C, double D, double minY, double maxY, EnumFacing fl, EnumFacing fw) {
		
		double[] boundingArray = new double[6];
		
		boundingArray[0] = fl==EnumFacing.WEST? A : fl==EnumFacing.EAST? B : fw==EnumFacing.EAST? C : D;
		boundingArray[1] = minY;
		boundingArray[2] = fl==EnumFacing.NORTH? A : fl==EnumFacing.SOUTH? B : fw==EnumFacing.SOUTH? C : D;
		boundingArray[3] = fl==EnumFacing.EAST? 1-A : fl==EnumFacing.WEST? 1-B : fw==EnumFacing.EAST? 1-D : 1-C;
		boundingArray[4] = maxY;
		boundingArray[5] = fl==EnumFacing.SOUTH? 1-A : fl==EnumFacing.NORTH? 1-B : fw==EnumFacing.SOUTH? 1-D : 1-C;
		
		return boundingArray;
		
	}
	
	private static MechanicalEnergy mechanicalEnergy = new MechanicalEnergy();
	
	public static MechanicalEnergy getMechanicalEnergy(World world, BlockPos startPos) {
		
		TileEntity tile = world.getTileEntity(startPos);
		TileEntity tileTransmitter;
		BlockPos pos = startPos;
		
		if(tile instanceof IMechanicalEnergy) {
			
			if(((IMechanicalEnergy) tile).isMechanicalEnergyReceiver()) {
				
				EnumFacing inputFacing = ((IMechanicalEnergy) tile).getMechanicalEnergyInputFacing();
				pos = startPos.offset(inputFacing, ((IMechanicalEnergy) tile).inputToCenterDistance()+1);
				tileTransmitter = world.getTileEntity(pos);
				
				if(tileTransmitter instanceof IMechanicalEnergy && ((IMechanicalEnergy) tileTransmitter).isMechanicalEnergyTransmitter() && (((IMechanicalEnergy) tileTransmitter).getMechanicalEnergyOutputFacing() == inputFacing.getOpposite())) {
					
					return new MechanicalEnergy(mechanicalEnergy.getTorque(), mechanicalEnergy.getSpeed(), mechanicalEnergy.getEnergy());
					
				}
				
			}
			
		}
		
		return new MechanicalEnergy(0,0,0);
		
	}
	
	public static void setMechanicalEnergy(MechanicalEnergy energy) {
		mechanicalEnergy = energy;
	}
	
}
