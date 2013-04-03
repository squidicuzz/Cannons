package at.pavlov.Cannons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Button;
import org.bukkit.material.Torch;

import at.pavlov.Cannons.cannon.Cannon;
import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.utils.CannonsUtil;

public class CannonManager
{
	private ArrayList<Cannon> CannonList = new  ArrayList<Cannon>();

	private Cannons plugin;
	private UserMessages message;
	private Config config;

	public CannonManager(Cannons cannons, UserMessages userMessages, Config config)
	{
		this.message = userMessages;
		this.config = config;
		this.plugin = cannons;
	}

	public class CannonAttribute
	{
		public Location barrel;
		public BlockFace face;
		public int barrel_length;
		public boolean find;
	}

	// ############### remove ###############################
	public void removeCannon(Location loc)
	{
		Cannon cannon = getCannon(loc);
		removeCannon(cannon);
	}
	
	// ############### remove ###############################
	public void removeCannon(Cannon cannon)
	{
		if (cannon != null)
		{
			// send message to the owner
			Player player = null;
			if (cannon.getOwner() != null)
			{
				player = Bukkit.getPlayer(cannon.getOwner());
			}
			if (player != null)
				player.sendMessage(message.cannonDestroyed);
			
			// destroy cannon (drops items, edit sign)
			cannon.destroyCannon();
			
			CannonList.remove(cannon);
		}
	}
	
	
	/**
	 * Checks if the name of a cannon is unique
	 * @param name
	 * @return true if the name is unique
	 */
	private boolean isCannonNameUnique(String name, String owner)
	{
		for (Cannon cannon : CannonList)
		{
			if (cannon.getName() != null && name != null)
			{
				if (cannon.getName().equals(name))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * generates a new unique cannon name
	 * @return
	 */
	private String newCannonName(String owner)
	{
		String[] nameList = {"Cannon"};//, "Artillery", "Gun"};
		
		Random r = new Random();
		String pre = nameList[r.nextInt(nameList.length)];

		for (int i=1; i < Integer.MAX_VALUE; i++)
		{
			String cannonName = pre + " " + i;

			if (isCannonNameUnique(cannonName, owner) == true)
			{
				return cannonName;
			}
		}
		
		
		return "no unique name";
	}


	// ############### createCannon ###############################
	private void createCannon(Cannon cannon, Player player)
	{
		CannonList.add(cannon);
		if (player != null)
			player.sendMessage(message.cannonBuilt);
	}
	
	/**
	 * get cannon by cannonName and Owner - used for Signs
	 * @param cannonName
	 * @return
	 */
	public Cannon getCannonFromStorage(String cannonName, String owner)
	{
		if (cannonName == null || owner == null) return null;
		
		for (Cannon cannon : CannonList)
		{
			if (cannonName.equals(cannon.getName()))
			{
				if (owner.equals(cannon.getOwner()))
				{
					return cannon;
				}
			}
			
		}
		return null;
	}
	
	
	/**
	 * Searches the storage if there is already a cannonblock on this location and returns the cannon
	 * @param loc
	 * @return
	 */
	private Cannon getCannonFromStorage(Location loc)
	{
		for (Cannon cannon : CannonList)
		{
			for (Location cannonBlock : cannon.getCannonBlocks())
			{
				if (cannonBlock.equals(loc))
				{
					return cannon;
				}
			}
		}
		return null;
	}

	/**
	 * searches for a cannon
	 * @param loc
	 * @return
	 */
	public Cannon getCannon(Location loc)
	{
		return getCannon(loc, null);
	}
	


	/**
	 * searches for a cannon and creates a new entry if it does not exist
	 * @param cannonBlock
	 * @param player
	 * @return
	 */
	public Cannon getCannon(Location cannonBlock, String owner)
	{
		Cannon cannon = getCannonFromStorage(cannonBlock);
		
		// this block is a part of an existing cannon 
		if (cannon != null)
		{
			// search cannon that is written on the sign
			Cannon cannonFromSign = getCannonFromStorage(cannon.getCannonNameFromSign(), cannon.getOwnerFromSign());

			// check if the name matches with the attached sign or if the name is not valid
			if (cannonFromSign == null || cannon.isCannonEqualSign() == true )
			{
				return cannon;
			}
			//else
			//{
				// different cannon, search the database for the right entry by the cannon name from the sign
			//	return cannonFromSign;
			//}
		}
		// else
		{
			// no existing cannon in storage -> check if there is a cannon
			Player player = null;
			if (owner != null)
			{
				player = Bukkit.getPlayer(owner);
			}
			CannonAttribute attribute = check_Cannon(cannonBlock, player);
			if (attribute.find == true)
			{
				// cannon found -> add cannon
				return addCannon(attribute, owner);
			}
		}

		return null;
	}

	/**
	 * returns true if this block is a part of a cannon
	 * @param loc
	 * @return
	 */
	public boolean isPartOfCannon(Location loc)
	{
		for (Cannon cannon : CannonList)
		{
			for (Location cannonblock : cannon.getCannonBlocks())
			{
				if (cannonblock.equals(loc))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	// ############### contains ###############################
	public boolean isCannonBlock(Location loc)
	{
		if (getCannon(loc) == null)
		{
			//no cannon block
			return false;
		}
		else
		{
			return true;
		}
	}


	// ############### getCannonAmount ###############################
	public int getCannonAmount(String player)
	{	
		int i = 1;
		for (Cannon cannon: CannonList)
		{
			if (cannon.getOwner() == null)
			{
				plugin.logSevere("Cannon has no owner. Contact the plugin developer");
			}
			else if (cannon.getOwner().equalsIgnoreCase(player))
			{
				i++;
			}
		}
		return i;
	}

	/**
	 * 
	 * @return List of cannons
	 */
	public List<Cannon> getCannonList()
	{
		return CannonList;
	}

	// ############### getCannonListSize ###############################
	public int getCannonListSize()
	{
		return CannonList.size();
	}


	// ############## CheckCannonsAmount ################################
	private boolean CheckCannonsAmount(Player player)
	{
		// the player is not valid - no limit check
		if (player == null) return true;
		if (player.getName() == null) return true;
		
		// both limitA/B and cannons.player.limit.5 work
		int newBuildLimit = getBuildLimit(player);
		int i = getCannonAmount(player.getName()); 

		if (newBuildLimit == -1)
		{
			// previous implementation
			if (player.hasPermission("cannons.player.limitB") && i > config.cannonLimitB)
			{
				player.sendMessage(message.getTooManyGuns(config.cannonLimitB));
				return false;
			}
			// limit B is stronger
			if (player.hasPermission("cannons.player.limitB") == false && player.hasPermission("cannons.player.limitA") && i > config.cannonLimitA)
			{
				player.sendMessage(message.getTooManyGuns(config.cannonLimitA));
				return false;
			}
			return true;
		}
		else
		{
			// new implementation
			if (i > newBuildLimit)
			{
				player.sendMessage(message.getTooManyGuns(newBuildLimit));
				return false;
			}
			return true;
		}
	}

	/**
	 * Extracts the build limit of a player
	 * 
	 * @param player
	 * @return
	 */
	private int getBuildLimit(Player player)
	{	
		//if all notes are enabled, set limit to a high number
		if(player.hasPermission("cannons.player.limit." + Integer.MAX_VALUE))
		{
			return Integer.MAX_VALUE;
		}
			
		//else check all nodes for the player
		for(int i = 100; i > 0; i--)
		{
			if(player.hasPermission("cannons.player.limit." + i))
				return i;
		}
		
		return -1;
	}

	// #################################### addCannonBlocks #########################
	private Cannon addCannonBlocks(Cannon cannon)
	{
		if (cannon == null)
			return null;

		// is cannonmaterial
		Block block = cannon.firingLocation.getBlock();
		BlockFace reverse = cannon.face.getOppositeFace();

		// button in front
		Location loc = block.getRelative(cannon.face).getLocation();
		cannon.addCannonBlock(loc);

		// barrel
		loc = block.getLocation();
		cannon.addCannonBlock(loc);

		// go back along the barrel + button backside
		int length = 1;
		do
		{
			length++;
			block = block.getRelative(reverse, 1);
			loc = block.getLocation();
			cannon.addCannonBlock(loc);
		} while (length < cannon.barrel_length + 1);
		block = block.getRelative(cannon.face, 1);

		// torch backside
		loc = block.getRelative(BlockFace.UP).getLocation();
		cannon.addCannonBlock(loc);

		return cannon;
	}

	// #################################### ADD_CANNON ######################
	private Cannon addCannon(CannonAttribute att_cannon, String owner)
	{
	
			// create a new cannon
			Cannon new_cannon = new Cannon();


				
			new_cannon.firingLocation = att_cannon.barrel;
			new_cannon.face = att_cannon.face;
			new_cannon.barrel_length = att_cannon.barrel_length;
			
			
			//search if there is a entry with this name written on the sign of the cannon with the same length
			Cannon old_cannon = getCannonFromStorage(new_cannon.getCannonNameFromSign(), new_cannon.getOwnerFromSign());

			//there is a cannon with this name in the storage -> update entry
			if (old_cannon != null && old_cannon.barrel_length == new_cannon.barrel_length)
			{
				
				old_cannon.firingLocation = new_cannon.firingLocation;
				old_cannon.face = new_cannon.face;

				
				// add cannon blocks
				old_cannon.setCannonBlocks(new ArrayList<Location>());
				old_cannon = addCannonBlocks(old_cannon);
				
				old_cannon.updateCannonSigns();
				return old_cannon;
			}
			//no cannon stored, this is a new cannon - make a new entry
			else
			{
				//there needs to be a player to create a cannon
				if (owner == null ) return null;
				
				Player player = Bukkit.getPlayer(owner);
				
				// check if player is allowed to build a new cannon
				boolean create = true;
				if (config.enableLimits == true)
				{
					create = CheckCannonsAmount(player);
				}
				if (create == true)
				{
					new_cannon.setOwner(owner);
					new_cannon.setName(newCannonName(owner));
					new_cannon.setLastFired(0);
					new_cannon.setLoadedGunpowder(0);
					new_cannon.setProjectileID(Material.AIR.getId());
					new_cannon.setProjectileData(0);
					new_cannon.setHorizontalAngle(0.0);
					new_cannon.setVerticalAngle(0.0);
					new_cannon.setDesignID(0);
					new_cannon.setValid(true);
				
					// add cannon blocks
					new_cannon = addCannonBlocks(new_cannon);
					// add cannon
					createCannon(new_cannon, player);

					new_cannon.updateCannonSigns();
					return new_cannon;
				}
			
			}

		return null;
	}

	

	

	// #################################### CHECK_CANNON ###########################
	private CannonAttribute check_Cannon(Location barrel, Player player)
	{
		boolean find_cannon = false;
		Block block = barrel.getBlock();
		BlockFace face = BlockFace.SELF;

		if (block.getType() == Material.TORCH)
		{
			Torch torch = (Torch) block.getState().getData();
			// get attached Block
			block = block.getRelative(torch.getAttachedFace());
		}
		else if (block.getType() == Material.STONE_BUTTON)
		{
			Button button = (Button) block.getState().getData();
			// get attached Block
			block = block.getRelative(button.getAttachedFace());
		}

		boolean redo = false;
		int length = 0, length_plus = 0, length_minus = 0;
		if (CannonsUtil.hasIdData(block, config.CannonMaterialId, config.CannonMaterialData))
		{
			do
			{
				// Select Barrel direction
				if (CannonsUtil.hasIdData(block.getRelative(BlockFace.EAST), config.CannonMaterialId, config.CannonMaterialData) && redo == false)
				{
					face = BlockFace.EAST;
				}
				else if (CannonsUtil.hasIdData(block.getRelative(BlockFace.WEST), config.CannonMaterialId, config.CannonMaterialData) && redo == false)
				{
					face = BlockFace.WEST;
				}
				else if (CannonsUtil.hasIdData(block.getRelative(BlockFace.SOUTH), config.CannonMaterialId, config.CannonMaterialData))
				{
					face = BlockFace.SOUTH;
				}
				else if (CannonsUtil.hasIdData(block.getRelative(BlockFace.NORTH), config.CannonMaterialId, config.CannonMaterialData))
				{
					face = BlockFace.NORTH;
				}
				else
				{
					face = BlockFace.NORTH;
				}

				// get barrel length
				do
				{
					length_plus++;
				} while (CannonsUtil.hasIdData(block.getRelative(face, length_plus), config.CannonMaterialId, config.CannonMaterialData) && length_plus < config.max_barrel_length);
				do
				{
					length_minus++;
				} while (CannonsUtil.hasIdData(block.getRelative(face.getOppositeFace(), length_minus), config.CannonMaterialId, config.CannonMaterialData) && length_minus < config.max_barrel_length);

				// Check Buttons and Torch
				if (CannonsUtil.CheckAttachedButton(block.getRelative(face.getOppositeFace(), length_minus - 1), face.getOppositeFace()))
				{
					if (CannonsUtil.CheckAttachedButton(block.getRelative(face, length_plus - 1), face))
					{
						if (CannonsUtil.CheckAttachedTorch(block.getRelative(face.getOppositeFace(), length_minus - 1)))
						{
							if (!CannonsUtil.CheckAttachedTorch(block.getRelative(face, length_plus - 1)))
							{
								// no change of Face necessary
								barrel = block.getRelative(face, length_plus - 1).getLocation();
								find_cannon = true;
							}
						}
						else if (CannonsUtil.CheckAttachedTorch(block.getRelative(face, length_plus - 1)))
						{
							if (!CannonsUtil.CheckAttachedTorch(block.getRelative(face.getOppositeFace(), length_minus - 1)))
							{
								face = face.getOppositeFace();
								barrel = block.getRelative(face, length_minus - 1).getLocation();
								find_cannon = true;
							}
						}
					}
				}
				

				length = length_plus + length_minus - 1;
				if (!(length >= config.min_barrel_length && length <= config.max_barrel_length && find_cannon == true))
				{
					find_cannon = false;
				}
				if (find_cannon == false && redo == false)
				{
					// try other direction
					redo = true;
					length_plus = 0;
					length_minus = 0;
				}
				else
				{
					redo = false;
				}

			} while (redo);
		}

		// check redstonetorches around the cannon
		if (find_cannon == true)
		{
			if (player != null)
			{
				// player is not allowed to place redstonetorch
				if (player.hasPermission("cannons.player.placeRedstoneTorch") == false)
				{
					// check all barrel blocks
					if (CheckRedstone(barrel, length, face) == true)
					{
						player.sendMessage(message.ErrorPermRestoneTorch);
					}
				}
			}
		}

		CannonAttribute cannon = new CannonAttribute();
		cannon.barrel = barrel;
		cannon.face = face;
		cannon.find = find_cannon;
		cannon.barrel_length = length;
		return cannon;
	}

	// ############## CheckRedstone ##############################
	private boolean CheckRedstone(Location barrel, int length, BlockFace face)
	{

		// torch
		Block barrelBlock = barrel.getBlock();
		Boolean broke = false;
		for (int i = 0; i < length; i++)
		{
			Block next = barrelBlock.getRelative(face.getOppositeFace(), i);
			for (Block b : SurroundingBlocks(next))
			{
				if (b.getType() == Material.REDSTONE_TORCH_ON || b.getType() == Material.REDSTONE_TORCH_OFF)
				{
					broke = true;
					b.breakNaturally();
				}
			}
		}

		// wire
		Block button = barrelBlock.getRelative(face.getOppositeFace(), length);
		for (Block b : HorizontalSurroundingBlocks(button))
		{
			if (b.getType() == Material.REDSTONE_WIRE)
			{
				broke = true;
				b.breakNaturally();
			}
		}

		return broke;
	}
	

	/**
	 * Deletes all cannons of this player in the database to reset the cannon limit
	 * @param player
	 */
	public void deleteCannons(String owner)
	{
		Iterator<Cannon> iter = CannonList.iterator();
		
		while(iter.hasNext())
		{
			Cannon next = iter.next();
			if (next.getOwner().equals(owner))
			{
				next.destroyCannon();
				iter.remove();
			}
		}
	}



	// ########### SurroundingBlocks #######################################
	public ArrayList<Block> SurroundingBlocks(Block block)
	{
		ArrayList<Block> Blocks = new ArrayList<Block>();

		Blocks.add(block.getRelative(BlockFace.UP));
		Blocks.add(block.getRelative(BlockFace.DOWN));
		Blocks.add(block.getRelative(BlockFace.SOUTH));
		Blocks.add(block.getRelative(BlockFace.WEST));
		Blocks.add(block.getRelative(BlockFace.NORTH));
		Blocks.add(block.getRelative(BlockFace.EAST));
		return Blocks;
	}

	// ########### SurroundingBlocks #######################################
	public ArrayList<Block> HorizontalSurroundingBlocks(Block block)
	{
		ArrayList<Block> Blocks = new ArrayList<Block>();

		Blocks.add(block.getRelative(BlockFace.SOUTH));
		Blocks.add(block.getRelative(BlockFace.WEST));
		Blocks.add(block.getRelative(BlockFace.NORTH));
		Blocks.add(block.getRelative(BlockFace.EAST));
		return Blocks;
	}

}