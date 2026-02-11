
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.*;

/** Solutions for Advent of Code 2020. */
sealed abstract class AOC_2020
{

	abstract void part1();
	abstract void part2();

   /**
    *
    * Fetches lines from the listed fileName, which must include extension at the end.
    *
    * @param   fileName    The name of the file that we are fetching the lines from.
    * @return              List of Strings, one String for each line. Line breaks not included.
    * @throw               If file cannot be found, or for other IO errors.
    *
    */
	Stream<String> fetchLines(String fileName)
	{
	
		try
		{
		
			return
				Files
				   .lines(Path.of("..", "input_files", fileName))
				   ;
		
		}
		
		catch (IOException ioe)
		{
		
			throw new IllegalArgumentException("Could not find the file titled " + fileName, ioe);
		
		}
	
	}

   /**
    *
    * Fetches lines from the listed fileName, which must include extension at the end.
    *
    * @param   fileName    The name of the file that we are fetching the lines from.
    * @return              List of Strings, one String for each line. Line breaks not included.
    * @throw               If file cannot be found, or for other IO errors.
    *
    */
	List<List<String>> fetchFromFileAsStringGrid(String fileName)
	{
	
		try
		{
		
			final List<String> list =
				Files
				   .lines(Path.of("..", "input_files", fileName))
				   .toList()
				   ;
		
			final List<List<String>> result = new ArrayList<>();
		
			for (final String each : list)
			{
			
				result.add(List.of(each.split("")));
			
			}
		
			return List.copyOf(result);
		
		}
		
		catch (IOException ioe)
		{
		
			throw new IllegalArgumentException("Could not find the file titled " + fileName, ioe);
		
		}
	
	}

	public static void main(String[] args)
	{
	
		new Day14().part2();
	
	}

	private static final class Day14 extends AOC_2020
	{
	
		public static final int NUMBER_OF_BITS = 36;
	
		sealed interface Value
		   permits
		      MaskValue,
		      WriteValue
		{
		
		
			static Value parseLine(final String line)
			{
			
				Objects.requireNonNull(line);
			
				if (line.isBlank())
				{
				
					throw new IllegalArgumentException("The line is blank!");
				
				}
			
				if (line.startsWith("mask"))
				{
				
					return MaskValue.parseLine(line);
				
				}
				
				else if (line.startsWith("mem"))
				{
				
					return WriteValue.parseLine(line);
				
				}
				
				else
				{
				
					throw new IllegalArgumentException("Unrecognized value! - " + line);
				
				}
			
			}
		
		}
	
		enum MaskEntry
		{
		
			ONE,
			ZERO,
			X,
			;
		
		}
	
		record MaskValue(List<MaskEntry> mask) implements Value
		{
		
			private static final Pattern REGEX = Pattern.compile("^mask \\= (?<mask>[X01]{36})$");
		
			MaskValue
			{
			
				Objects.requireNonNull(mask);
			
				if (mask.isEmpty())
				{
				
					throw new IllegalArgumentException("mask is empty!");
				
				}
			
				if (mask.stream().anyMatch(Objects::isNull))
				{
				
					throw new IllegalArgumentException("mask cannot contain nulls!");
				
				}
			
			}
		
			private static MaskValue parseLine(final String line)
			{
			
				Objects.requireNonNull(line);
			
				if (line.isBlank())
				{
				
					throw new IllegalArgumentException("line is blank! - " + line);
				
				}
			
				final Matcher matcher = REGEX.matcher(line);
			
				if (matcher.matches())
				{
				
					final String rawMask = matcher.group("mask");
				
					Objects.requireNonNull(rawMask);
				
					if (rawMask.isBlank())
					{
					
						throw new IllegalArgumentException("mask is blank! - mask");
					
					}
				
					return
						new MaskValue
						(
						   rawMask
						      .chars()
						      .mapToObj
						      (
						         each ->
										switch (each)
										{
										
											case  '0'   -> MaskEntry.ZERO;
											case  '1'   -> MaskEntry.ONE;
											case  'X'   -> MaskEntry.X;
											default     -> throw new IllegalStateException("? - " + each);
										
										}
						      )
						      .toList()
						)
						;
				
				}
				
				else
				{
				
					throw new IllegalArgumentException("line does not match the regex! - " + line);
				
				}
			
			}
		
		}
	
		record WriteValue(BigInteger index, BigInteger rawValue) implements Value
		{
		
			private static final Pattern REGEX = Pattern.compile("^mem\\[(?<index>\\d+)\\]\\s+\\=\\s+(?<rawValue>\\d+)$");
		
			WriteValue
			{
			
				Objects.requireNonNull(index);
				Objects.requireNonNull(rawValue);
			
			}
		
			private static WriteValue parseLine(final String line)
			{
			
				VALIDATION:
				{
				
					Objects.requireNonNull(line);
				
					if (line.isBlank())
					{
					
						throw new IllegalArgumentException("line cannot be blank!");
					
					}
				
				}
			
				final BigInteger index;
				final BigInteger rawValue;
			
				EXTRACT:
				{
				
					final Matcher matcher = REGEX.matcher(line);
				
					if (!matcher.matches())
					{
					
						throw new IllegalArgumentException("Invalid line! line = " + line);
					
					}
				
					final String rawIndex = matcher.group("index");
					final String rawRawValue = matcher.group("rawValue");
				
					index = new BigInteger(rawIndex);
					rawValue = new BigInteger(rawRawValue);
				
				}
			
				return new WriteValue(index, rawValue);
			
			}
		
		}
	
		void part1()
		{
		
			record WriteValuesWithMask(MaskValue mask, List<WriteValue> writeValues)
			{
			
				WriteValuesWithMask
				{
				
					Objects.requireNonNull(mask);
					Objects.requireNonNull(writeValues);
				
					if (writeValues.stream().anyMatch(Objects::isNull))
					{
					
						throw new IllegalArgumentException("writeValues cannot contain nulls! writeValues = " + writeValues);
					
					}
				
				}
			
				public static Optional<WriteValuesWithMask> parse(final List<Value> values)
				{
				
					Objects.requireNonNull(values);
				
					if (values.isEmpty())
					{
					
						return Optional.empty();
					
					}
				
					if (!(values.getFirst() instanceof final MaskValue maskValue))
					{
					
						throw new IllegalArgumentException("First element must be a MaskValue! values = " + values);
					
					}
				
					final List<Value> writeValues = values.subList(1, values.size());
				
					if (!writeValues.stream().allMatch(WriteValue.class::isInstance))
					{
					
						throw new IllegalArgumentException("writeValues can only contain WriteValue! writeValues = " + writeValues);
					
					}
				
					return Optional.of(new WriteValuesWithMask(maskValue, writeValues.stream().map(WriteValue.class::cast).toList()));
				
				}
			
				public void writeValuesTo(final Map<BigInteger, BigInteger> map)
				{
				
					Objects.requireNonNull(map);
				
					System.out.println(this);
				
					for (final WriteValue eachValue : this.writeValues)
					{
					
						BigInteger constructedValue = eachValue.rawValue();
					
						System.out.println(NUMBER_OF_BITS);
					
						for (int i = 0; i < NUMBER_OF_BITS; i++)
						{
						
							System.out.print(constructedValue + " --> ");
						
							final var maskBlah = this.mask.mask().reversed().get(i);
						
							System.out.print(maskBlah + " --> " + i + " --> ");
						
							constructedValue =
								switch (maskBlah)
								{
								
									case  ONE   -> constructedValue.setBit(i);
									case  ZERO  -> constructedValue.clearBit(i);
									case  X     -> constructedValue;
								
								}
								;
						
							System.out.println(constructedValue);
						
						}
					
						map.put(eachValue.index(), constructedValue);
					
						System.out.println(map);
					
							new Object()
							{
							
								public void blah(){}
							
							};
					
					}
				
				}
			
			}
		
			final List<WriteValuesWithMask> windows;
		
			FETCH_INPUT:
			{
			
				final List<String> testLines =
					"""
					mask = 000000000000000000000000000000X1001X
					mem[8] = 11
					mem[7] = 101
					mem[8] = 0
					"""
					   .lines()
					   .toList()
					   ;
			
				final List<String> rawInput =
					this
					   .fetchLines("day14.txt")
					   .toList()
					   ;
			
				final List<Value> values =
					rawInput
					// testLines
					   .stream()
					   .map(Value::parseLine)
					   .toList()
					   ;
			
				windows =
					values
					   .stream()
					   .gather(windowBy(WriteValue.class::isInstance))
					   .map(WriteValuesWithMask::parse)
					   .filter(Optional::isPresent)
					   .map(Optional::orElseThrow)
					   .toList()
					   ;
			
			}
		
			final Map<BigInteger, BigInteger> finalValues = new HashMap<>();
		
			windows.forEach(eachWindow -> eachWindow.writeValuesTo(finalValues));
		
			System.out.println(finalValues);
		
			final BigInteger finalAnswer =
				finalValues
				   .values()
				   .stream()
				   .reduce(BigInteger::add)
				   .orElseThrow()
				   ;
		
			System.out.println(finalAnswer);
		
		}
	
		void part2()
		{
		
			record WriteValuesWithMask(MaskValue mask, List<WriteValue> writeValues)
			{
			
				WriteValuesWithMask
				{
				
					Objects.requireNonNull(mask);
					Objects.requireNonNull(writeValues);
				
					if (writeValues.stream().anyMatch(Objects::isNull))
					{
					
						throw new IllegalArgumentException("writeValues cannot contain nulls! writeValues = " + writeValues);
					
					}
				
				}
			
				public static Optional<WriteValuesWithMask> parse(final List<Value> values)
				{
				
					Objects.requireNonNull(values);
				
					if (values.isEmpty())
					{
					
						return Optional.empty();
					
					}
				
					if (!(values.getFirst() instanceof final MaskValue maskValue))
					{
					
						throw new IllegalArgumentException("First element must be a MaskValue! values = " + values);
					
					}
				
					final List<Value> writeValues = values.subList(1, values.size());
				
					if (!writeValues.stream().allMatch(WriteValue.class::isInstance))
					{
					
						throw new IllegalArgumentException("writeValues can only contain WriteValue! writeValues = " + writeValues);
					
					}
				
					return Optional.of(new WriteValuesWithMask(maskValue, writeValues.stream().map(WriteValue.class::cast).toList()));
				
				}
			
				public void writeValuesTo(final Map<BigInteger, BigInteger> map)
				{
				
					Objects.requireNonNull(map);
				
					System.out.println(this);
				
					for (final WriteValue eachValue : this.writeValues)
					{
					
						BigInteger constructedAddress = eachValue.index();
					
						for (int i = 0; i < NUMBER_OF_BITS; i++)
						{
						
							System.out.print(constructedAddress + " --> ");
						
							final var maskBlah = this.mask.mask().reversed().get(i);
						
							System.out.print(maskBlah + " --> " + i + " --> ");
						
							constructedAddress =
								switch (maskBlah)
								{
								
									case  ZERO  -> constructedAddress;
									case  ONE   -> constructedAddress.setBit(i);
									case  X     -> throw new UnsupportedOperationException("unfinished");
								
								}
								;
						
							System.out.println(constructedAddress);
						
						}
					
						map.put(eachValue.index(), constructedAddress);
					
						System.out.println(map);
					
					}
				
				}
			
			}
		
			final List<WriteValuesWithMask> windows;
		
			FETCH_INPUT:
			{
			
				final List<String> testLines =
					"""
					mask = 000000000000000000000000000000X1001X
					mem[42] = 100
					mask = 00000000000000000000000000000000X0XX
					mem[26] = 1
					"""
					   .lines()
					   .toList()
					   ;
			
				final List<String> rawInput =
					this
					   .fetchLines("day14.txt")
					   .toList()
					   ;
			
				final List<Value> values =
					// rawInput
					testLines
					   .stream()
					   .map(Value::parseLine)
					   .map(eachValue -> eachValue instanceof MaskValue mv ? new MaskValue(mv.mask().reversed()) : eachValue)
					   .toList()
					   ;
			
				windows =
					values
					   .stream()
					   .gather(windowBy(WriteValue.class::isInstance))
					   .map(WriteValuesWithMask::parse)
					   .filter(Optional::isPresent)
					   .map(Optional::orElseThrow)
					   .toList()
					   ;
			
			}
		
			final Map<BigInteger, BigInteger> finalValues = new HashMap<>();
		
			windows
				.stream()
				.forEach
				(
					eachWindow ->
					{
					
						final MaskValue maskValue = eachWindow.mask();
						final List<WriteValue> writeValues = eachWindow.writeValues();
					   
						writeValues
							.forEach
							(
								eachWriteValue ->
								{
								
									final BigInteger index = eachWriteValue.index();
									final BigInteger rawValue = eachWriteValue.rawValue();
								
									for (int i = 0; i < index.bitLength(); i++)
									{
									
										final MaskEntry maskBit = maskValue.mask().get(i);
									
									}
								
								}
							)
							;
					
					}
				)
				;
		   
		
		}
	
	}

	private static final class Day13 extends AOC_2020
	{
	
		void part1()
		{
		
			final long earliestTimestamp;
			final SortedSet<Long> busIds;
		
			fetchRelevantData:
			{
			
				final List<String> schedule =
					this
					   .fetchLines("day13.txt")
					   .toList()
					   ;
			
				final List<Long> contents =
					Arrays
					   .asList(schedule.get(1).split(","))
					   .stream()
					   .filter(each -> !each.equals("x"))
					   .map(Long::parseLong)
					   .toList()
					   ;
			
				final SortedSet<Long> tempLong = new TreeSet<>(contents);
			
				earliestTimestamp = Long.parseLong(schedule.get(0));
				busIds = Collections.unmodifiableSortedSet(tempLong);
			
			}
		
			System.out.println(earliestTimestamp);
			System.out.println(busIds);
		
			for (final long busId : busIds)
			{
			
				final long waitDuration = busId - (earliestTimestamp % busId);
			
				System.out.println("waitDuration = " + waitDuration + " ---- busId = " + busId );
			
			}
		
		}
	
		static sealed interface BusType
		{
		
			int index();
		
			static BusType parse(String value, int index)
			{
			
				Objects.requireNonNull(value);
			
				if (value.matches("\\d+"))
				{
				
					return new KnownId(Integer.parseInt(value), index);
				
				}
				
				else
				{
				
					return new UnknownId(index);
				
				}
			
			}
		
		}
	
		static record KnownId(int id, int index) implements BusType, Comparable<KnownId>
		{
		
			static final Comparator<KnownId> COMPARATOR =
			   Comparator
			      .comparingInt(KnownId::index)
			      ;
		
			@Override
			public int compareTo(final KnownId other)
			{
			
				Objects.requireNonNull(other);
			
				return COMPARATOR.compare(this, other);
			
			}
		
			int startingPoint()
			{
			
				return this.index();
			
			}
		
			int rateOfIncrease()
			{
			
				return this.id();
			
			}
		
		}
	
		static record UnknownId(int index) implements BusType {}
	
		private Stream<String> stream(String... strings)
		{
		
			return Stream.of(strings);
		
		}
	
		void part2()
		{
		
			final SortedSet<KnownId> buses;
		
			fetchRelevantData:
			{
			
				final String TEST_DATA =
					// "17,x,13,19"            // answer is 3417
					// "67,7,59,61"            // answer is 754018
					// "67,x,7,59,61"          // answer is 779210
					// "67,7,x,59,61"          // answer is 1261476
					// "1789,37,47,1889"       // answer is 1202161486
					// "7,13,x,x,59,x,31,19"   // answer is 1068781
					""
					;
			
				final List<String> schedule =
					this
					   .fetchLines("day13.txt")
					   // .stream("123\n", TEST_DATA)
					   .toList()
					   ;
			
				final List<String> input = Arrays.asList(schedule.get(1).split(","));
			
				final List<BusType> contents = new ArrayList<>();
			
				for (int i = 0; i < input.size(); i++)
				{
				
					contents.add(BusType.parse(input.get(i), i));
				
				}
			
				buses =
					Collections
					   .unmodifiableSortedSet
					   (
					      contents
					         .stream()
					         .filter(KnownId.class::isInstance)
					         .map(KnownId.class::cast)
					         .collect(Collectors.toCollection(TreeSet::new))
					   )
					   ;
			
			}
		
			System.out.println("---\n" + LocalDateTime.now());
			buses.forEach(System.out::println);
		
			final BigInteger ZERO = BigInteger.ZERO;
			BigInteger first = ZERO;
			BigInteger second = ZERO;
			BigInteger stepSize = ZERO;
			BigInteger finalAnswer = null;
		
			int busIndex = 0;
		
			mainLoop:
			for (final KnownId eachId : buses)
			{
			
				final BigInteger index = BigInteger.valueOf(eachId.index());
				final BigInteger id    = BigInteger.valueOf(eachId.id());
			
				if (first.equals(ZERO) && second.equals(ZERO) && stepSize.equals(ZERO))
				{
				
					first = index;
					stepSize = id;
					second = first.add(stepSize);
				
					continue mainLoop;
				
				}
				
				else
				{
				
					while (!first.add(index).mod(id).equals(ZERO) || first.compareTo(id) < 0)
					{
					
						first = first.add(stepSize);
					
					}
				
					busIndex++;
				
					if (busIndex == buses.size() - 1)
					{
					
						finalAnswer = first;
						break mainLoop;
					
					}
					
					else
					{
					
						second = first;
					
						do
						{
						
							second = second.add(stepSize);
						
						}
						
						while (!second.add(index).mod(id).equals(ZERO));
					
						stepSize = second.subtract(first);
					
					}
				
				}
			
			}
		
			System.out.println("finalAnswer\t" + finalAnswer);
		
		}
	
	}

	private static final class Day12 extends AOC_2020
	{
	
		private enum Direction
		{
		
			NORTH,
			SOUTH,
			EAST,
			WEST,
			;
		
			Direction turnLeft()
			{
			
				return
					switch (this)
					{
					
						case  NORTH -> WEST;
						case  WEST  -> SOUTH;
						case  SOUTH -> EAST;
						case  EAST  -> NORTH;
					
					};
			
			}
		
			Direction turnRight()
			{
			
				return
					switch (this)
					{
					
						case  NORTH -> EAST;
						case  EAST  -> SOUTH;
						case  SOUTH -> WEST;
						case  WEST  -> NORTH;
					
					};
			
			}
		
		}
	
	   /** Day 12 - part 1 */
		void part1()
		{
		
			final List<String> instructions =
				this
				   .fetchLines("day12.txt")
				   .toList()
				   ;
		
			int row = 0;
			int column = 0;
			Direction direction = Direction.EAST;
		
			for (final String instruction : instructions)
			{
			
				final char action = instruction.charAt(0);
				final int value = Integer.parseInt(instruction.substring(1));
			
				switch (action)
				{
				
					case  'N'   -> row      -= value;
					case  'S'   -> row      += value;
					case  'E'   -> column   += value;
					case  'W'   -> column   -= value;
					case  'L'   ->
						direction =
							switch (value)
							{
							
								case  90    -> direction.turnLeft();
								case  180   -> direction.turnLeft().turnLeft();
								case  270   -> direction.turnRight();
								default     -> throw new IllegalArgumentException("bad value for turning -- " + value);
							
							}
							;
					case  'R'   ->
						direction =
							switch (value)
							{
							
								case  90    -> direction.turnRight();
								case  180   -> direction.turnRight().turnRight();
								case  270   -> direction.turnLeft();
								default     -> throw new IllegalArgumentException("bad value for turning -- " + value);
							
							}
							;
					case  'F'   ->
					{
					
						switch (direction)
						{
						
							case  NORTH -> row      -= value;
							case  SOUTH -> row      += value;
							case  EAST  -> column   += value;
							case  WEST  -> column   -= value;
						
						}
					
					}
				
				}
			
			}
		
			System.out.println(row);
			System.out.println(column);
			System.out.println(direction);
		
		}
	
		void part2()
		{
		
			final List<String> instructions =
				this
				   .fetchLines("day12.txt")
				   .toList()
				   ;
		
			int shipRow = 0;
			int shipColumn = 0;
			int wayPointRow = -1;
			int wayPointColumn = 10;
		
			Direction direction = Direction.EAST;
		
			for (final String instruction : instructions)
			{
			
				final char action = instruction.charAt(0);
				final int value = Integer.parseInt(instruction.substring(1));
			
				switch (action)
				{
				
					case  'N'   -> wayPointRow    -= value;
					case  'S'   -> wayPointRow    += value;
					case  'E'   -> wayPointColumn += value;
					case  'W'   -> wayPointColumn -= value;
					case  'L'   ->
					{
					
						switch (value)
						{
						
							case  90    ->
							{
							
								final int deltaRow = wayPointRow - shipRow;
								final int deltaColumn = wayPointColumn - shipColumn;
							
								wayPointRow = shipRow - deltaColumn;
								wayPointColumn = shipColumn + deltaRow;
							
							}
						
							case  180   ->
							{
							
								final int deltaRow = shipRow - wayPointRow;
								final int deltaColumn = shipColumn - wayPointColumn;
							
								wayPointRow = shipRow + deltaRow;
								wayPointColumn = shipColumn + deltaColumn;
							
							}
						
							case  270   ->
							{
							
								final int deltaRow = wayPointRow - shipRow;
								final int deltaColumn = wayPointColumn - shipColumn;
							
								wayPointRow = shipRow + deltaColumn;
								wayPointColumn = shipColumn - deltaRow;
							
							}
						
							default     ->
								throw new IllegalArgumentException("bad value for turning -- " + value);
						
						}
					
					}
				
					case  'R'   ->
					{
					
						switch (value)
						{
						
							case  90    ->
							{
							
								final int deltaRow = wayPointRow - shipRow;
								final int deltaColumn = wayPointColumn - shipColumn;
							
								wayPointRow = shipRow + deltaColumn;
								wayPointColumn = shipColumn - deltaRow;
							
							}
						
							case  180   ->
							{
							
								final int deltaRow = shipRow - wayPointRow;
								final int deltaColumn = shipColumn - wayPointColumn;
							
								wayPointRow = shipRow + deltaRow;
								wayPointColumn = shipColumn + deltaColumn;
							
							}
						
							case  270   ->
							{
							
								final int deltaRow = wayPointRow - shipRow;
								final int deltaColumn = wayPointColumn - shipColumn;
							
								wayPointRow = shipRow - deltaColumn;
								wayPointColumn = shipColumn + deltaRow;
							
							}
						
							default     ->
								throw new IllegalArgumentException("bad value for turning -- " + value);
						
						}
					
					}
				
					case  'F'   ->
					{
					
						final int deltaRow = wayPointRow - shipRow;
						final int deltaColumn = wayPointColumn - shipColumn;
					
						wayPointRow = wayPointRow + (deltaRow * value);
						wayPointColumn = wayPointColumn + (deltaColumn * value);
						shipRow = shipRow + (deltaRow * value);
						shipColumn = shipColumn + (deltaColumn * value);
					
					}
				
				}
			
			}
		
			System.out.println("shipRow = " + shipRow);
			System.out.println("shipColumn = " + shipColumn);
			System.out.println("wayPointRow = " + wayPointRow);
			System.out.println("wayPointColumn = " + wayPointColumn);
		
		}
	
	}

	private static final class Day11 extends AOC_2020
	{
	
	   /** Day 11 - part 2 */
		void part2()
		{
		
			final List<List<String>> grid =
				this
				   .fetchFromFileAsStringGrid("day11.txt")
				   ;
		
			final int rowLimit = grid.size();
		
			List<List<String>> oldGrid = new ArrayList<>();
			List<List<String>> newGrid = grid;
		
			int iteration = 1;
		
			record Grid(List<List<String>> temp)
			{
			
				int numberOfOccupiedSeats()
				{
				
					int countOfSeats = 0;
				
					for (final List<String> eachRow : temp)
					{
					
						for (final String eachCell : eachRow)
						{
						
							if ("#".equals(eachCell))
							{
							
								countOfSeats++;
							
							}
						
						}
					
					}
				
					return countOfSeats;
				
				}
			
				public String toString()
				{
				
					String output = "";
				
					for (final List<String> eachRow : temp)
					{
					
						for (final String eachCell : eachRow)
						{
						
							output+= eachCell;
						
						}
					
						output += "\n";
					
					}
				
					return output;
				
				}
			
			}
		
			while (!oldGrid.equals(newGrid))
			{
			
				System.out.println("Iteration " + iteration++);
			
				oldGrid = newGrid;
				newGrid = new ArrayList<>();
			
				for (int row = 0; row < rowLimit; row++)
				{
				
					newGrid.add(new ArrayList<>());
				
					final int columnLimit = oldGrid.get(row).size();
				
					for (int column = 0; column < columnLimit; column++)
					{
					
						final String newCharacter = day11_2_calculateNewValue(oldGrid, row, column, rowLimit, columnLimit);
					
						newGrid.get(row).add(column, newCharacter);
					
					}
				
				}
			
				var newViewer = new Grid(newGrid);
			
				System.out.print("");
			
			}
		
			System.out.println("done");
			System.out.println(new Grid(newGrid).numberOfOccupiedSeats());
		
		}
	
		private String day11_2_calculateNewValue(List<List<String>> grid, int row, int column, int rowLimit, int columnLimit)
		{
		
			final String currentCell = grid.get(row).get(column);
		
			if (currentCell.equals("."))
			{
			
				return currentCell;
			
			}
		
			int surroundingNumberOfPeople = 0;
		
			final IntUnaryOperator plusOne = input -> input + 1;
			final IntUnaryOperator minusOne = input -> input - 1;
			final IntUnaryOperator noChange = IntUnaryOperator.identity();
		
		
		   /** N  */
			surroundingNumberOfPeople += this.day11_2_checkDirection(grid, row, column, rowLimit, columnLimit, minusOne,   noChange);
		   /** NE */
			surroundingNumberOfPeople += this.day11_2_checkDirection(grid, row, column, rowLimit, columnLimit, minusOne,   plusOne);
		   /** E  */
			surroundingNumberOfPeople += this.day11_2_checkDirection(grid, row, column, rowLimit, columnLimit, noChange,   plusOne);
		   /** SE */
			surroundingNumberOfPeople += this.day11_2_checkDirection(grid, row, column, rowLimit, columnLimit, plusOne,    plusOne);
		   /** S  */
			surroundingNumberOfPeople += this.day11_2_checkDirection(grid, row, column, rowLimit, columnLimit, plusOne,    noChange);
		   /** SW */
			surroundingNumberOfPeople += this.day11_2_checkDirection(grid, row, column, rowLimit, columnLimit, plusOne,    minusOne);
		   /** W  */
			surroundingNumberOfPeople += this.day11_2_checkDirection(grid, row, column, rowLimit, columnLimit, noChange,   minusOne);
		   /** NW */
			surroundingNumberOfPeople += this.day11_2_checkDirection(grid, row, column, rowLimit, columnLimit, minusOne,   minusOne);
		
			if (currentCell.equals("L") && surroundingNumberOfPeople == 0)
			{
			
				return "#";
			
			}
			
			else if (currentCell.equals("#") && surroundingNumberOfPeople >= 5)
			{
			
				return "L";
			
			}
			
			else
			{
			
				return currentCell;
			
			}
		
		}
	
		int day11_2_checkDirection(List<List<String>> grid, int row, int column, int rowLimit, int columnLimit, IntUnaryOperator rowModifier, IntUnaryOperator columnModifier)
		{
		
			final String currentCell = grid.get(row).get(column);
		
			int nextRow = rowModifier.applyAsInt(row);
			int nextColumn = columnModifier.applyAsInt(column);
		
			stepLoop:
			while (this.day11_1_isAdjacent(nextRow, nextColumn, rowLimit, columnLimit, row, column))
			{
			
				final String nextCell = grid.get(nextRow).get(nextColumn);
			
				nextRow = rowModifier.applyAsInt(nextRow);
				nextColumn = columnModifier.applyAsInt(nextColumn);
			
				if (nextCell.equals("."))
				{
				
					continue stepLoop;
				
				}
				
				else if (nextCell.equals("#"))
				{
				
					return 1;
				
				}
				
				else if (nextCell.equals("L"))
				{
				
					return 0;
				
				}
				
				else
				{
				
					throw new IllegalArgumentException("Unexpected value! nextCell = " + nextCell);
				
				}
			
			}
		
			return 0;
		
		}
	
	   /** Day 11 - part 1 */
		void part1()
		{
		
			final List<List<String>> grid =
				this.fetchFromFileAsStringGrid("day11.txt");
		
			final int rowLimit = grid.size();
		
			List<List<String>> oldGrid = new ArrayList<>();
			List<List<String>> newGrid = grid;
		
			int iteration = 1;
		
			record Grid(List<List<String>> temp)
			{
			
				int numberOfOccupiedSeats()
				{
				
					int countOfSeats = 0;
				
					for (List<String> eachRow : temp)
					{
					
						for (String eachCell : eachRow)
						{
						
							if (eachCell.equals("#"))
							{
							
								countOfSeats++;
							
							}
						
						}
					
					}
				
					return countOfSeats;
				
				}
			
				public String toString()
				{
				
					String output = "";
				
					for (List<String> eachRow : temp)
					{
					
						for (String eachCell : eachRow)
						{
						
							output+= eachCell;
						
						}
					
						output += "\n";
					
					}
				
					return output;
				
				}
			
			}
		
			while (!oldGrid.equals(newGrid))
			{
			
				System.out.println("Iteration " + iteration++);
			
				oldGrid = newGrid;
				newGrid = new ArrayList<>();
			
				for (int row = 0; row < rowLimit; row++)
				{
				
					newGrid.add(new ArrayList<>());
				
					final int columnLimit = oldGrid.get(row).size();
				
					for (int column = 0; column < columnLimit; column++)
					{
					
						final String newCharacter = day11_1_calculateNewValue(oldGrid, row, column, rowLimit, columnLimit);
					
						newGrid.get(row).add(column, newCharacter);
					
					}
				
				}
			
				var newViewer = new Grid(newGrid);
			
				System.out.print("");
			
			}
		
			System.out.println("done");
			System.out.println(new Grid(newGrid).numberOfOccupiedSeats());
		
		}
	
		private String day11_1_calculateNewValue(List<List<String>> grid, int row, int column, int rowLimit, int columnLimit)
		{
		
			final String currentCell = grid.get(row).get(column);
		
			if (currentCell.equals("."))
			{
			
				return currentCell;
			
			}
		
			int surroundingNumberOfPeople = 0;
		
			for (int nextRow = row - 1; nextRow <= row + 1; nextRow++)
			{
			
				for (int nextColumn = column - 1; nextColumn <= column + 1; nextColumn++)
				{
				
					if (this.day11_1_isAdjacent(nextRow, nextColumn, rowLimit, columnLimit, row, column))
					{
					
						final String adjacentCell = grid.get(nextRow).get(nextColumn);
					
						if (adjacentCell.equals("#"))
						{
						
							surroundingNumberOfPeople++;
						
						}
					
					}
				
				}
			
			}
		
			if (currentCell.equals("L") && surroundingNumberOfPeople == 0)
			{
			
				return "#";
			
			}
			
			else if (currentCell.equals("#") && surroundingNumberOfPeople >= 4)
			{
			
				return "L";
			
			}
			
			else
			{
			
				return currentCell;
			
			}
		
		}
	
		private boolean day11_1_isAdjacent(int nextRow, int nextColumn, int rowLimit, int columnLimit, int row, int column)
		{
		
			return
				nextRow < rowLimit
				&& nextRow >= 0
				&& nextColumn < columnLimit
				&& nextColumn >= 0
				&&
				(
				      nextRow != row
				   || nextColumn != column
				)
				;
		
		}
	
	}

	private static final class Day10 extends AOC_2020
	{
	
	   /** Day 10 - part 2 */
		void part2()
		{
		
			final List<Long> lines =
				Stream
				.concat
				(
				   this.fetchLines("day10.txt"),
				   Stream.of("0")
				)
				.mapToLong(Long::parseLong)
				.sorted()
				.boxed()
				.toList()
				;
		
			final Map<Integer, Long> pathsPerIndex = new HashMap<>();
		
			for (int i = lines.size() - 1; i >= 0; i--)
			{
			
				long count = 0;
			
				for (int j = i + 1; j < lines.size() && j <= i + 3; j++)
				{
				
					final long start = lines.get(i);
					final long next  = lines.get(j);
				
					if ((next - start) <= 3 && pathsPerIndex.containsKey(j))
					{
					
						count += pathsPerIndex.get(j);
					
					}
				
				}
			
				pathsPerIndex.put(i, (count == 0 ? 1 : count));
			
			}
		
			System.out.println(pathsPerIndex);
		
		}
	
	   /** Day 10 - part 1 */
		void part1()
		{
		
			List<Long> lines =
				//LongStream.of(28, 33, 18, 42, 31, 14, 46, 20, 48, 47, 24, 23, 49, 45, 19, 38, 39, 11, 1, 32, 25, 35, 8, 17, 7, 9, 4, 2, 34, 10, 3)
				this.
				fetchLines("day10.txt")
				   .mapToLong(Long::parseLong)
				   .sorted()
				   .boxed()
				   .toList()
				   ;
		
			Map<Integer, List<Long>> histogram =
				Map.ofEntries(
				   Map.entry(1, new ArrayList<>()),
				   Map.entry(2, new ArrayList<>()),
				   Map.entry(3, new ArrayList<>())
				)
				;
		
			long previous = 0;
		
			for (long each : lines)
			{
			
				final int diff = (int)(each - previous);
			
				if (diff >= 1 && diff <= 3)
				{
				
					histogram.get(diff).add(each);
				
				}
				
				else
				{
				
					throw new IllegalStateException(each + " is an unacceptable value because it is out of range from " + previous);
				
				}
			
				previous = each;
			
			}
		
		   /** Perform the final hop - a 3 point jolt bounce when connecting to the laptop. */
			List<Long> list1 = histogram.get(1);
			List<Long> list3 = histogram.get(3);
		
			long list1Max = Collections.max(list1);
			long list3Max = Collections.max(list3);
		
			long max = Math.max(list1Max, list3Max);
		
			histogram.get(3).add(max + 3);
		
		   /** Print results. */
			System.out.println(histogram);
		
			System.out.println("1 - " + list1.size());
			System.out.println("3 - " + list3.size());
		
			long answer = list1.size() * list3.size();
		
			System.out.println(answer);
		
		}
	
	}

	private static final class Day1 extends AOC_2020
	{
	
		void part1()
		{
		
			assert 1 == 0 : "WHAT";
		
			final List<Integer> listOfNumbers =
				this.
				   fetchLines("day1.txt")
				      .map(Integer::parseInt)
				      .toList()
				      ;
		
			final int length = listOfNumbers.size();
		
			final long result = 123;
		
			record Pair(Integer first, Integer second)
			{
			
				Pair map(final List<Integer> listOfNumbers)
				{
				
					final int firstValue = listOfNumbers.get(first);
					final int secondValue = listOfNumbers.get(second);
				
					return new Pair(firstValue, secondValue);
				
				}
			
			}
		
			final var intermediate =
				IntStream
				   .range(0, length)
				   .boxed()
				   .flatMap
				   (
				      firstIndex ->
				         IntStream
				            .range(firstIndex, length)
				            .mapToObj(secondIndex -> new Pair(firstIndex, secondIndex))
				   )
				   .filter(indexPair -> !indexPair.first().equals(indexPair.second()))
				   .map(indexPair -> indexPair.map(listOfNumbers))
				   .filter(valuePair -> 2020 == (valuePair.first() + valuePair.second()))
				   .limit(3)
				   .toList()
				   ;
		
			System.out.println(intermediate);
		
		}
	
		void part2()
		{}
	
	}

	/** It is sequential, out of necessity. What difference is there between this and split() if not order? */
	void unfold()
	{
	
		final String input = "01X10X00X11X";
	   
		class State
		{
		
			private final List<String> queue = new ArrayList<>();
		   
			boolean integrate(final String element, Gatherer.Downstream<? super List<String>> downstream)
			{
			
			
			
			}
		
		}
	
	}

	<TR> Gatherer<TR, ?, List<TR>> windowBy(Predicate<TR> includeInCurrentWindow) {
		class State {
			ArrayList<TR> window;
		
			boolean integrate(TR element, Gatherer.Downstream<? super List<TR>> downstream) {
				if (window != null && !includeInCurrentWindow.test(element)) {
					var result = Collections.unmodifiableList(window);
					window = null;
					if (!downstream.push(result))
						return false;
				}
			
				if (window == null)
					window = new ArrayList<>();
			
				return window.add(element);
			}
		
			void finish(Gatherer.Downstream<? super List<TR>> downstream) {
				if (window != null) {
					var result = Collections.unmodifiableList(window);
					window = null;
					downstream.push(result);
				}
			}
		}
		return Gatherer.<TR, State, List<TR>>ofSequential(State::new, State::integrate, State::finish);
	}

}