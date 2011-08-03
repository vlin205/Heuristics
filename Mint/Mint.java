import java.util.*;

public class Mint
{
	final static int N = 60;
	static int score = 999999;
	static int exchangeScore = 999999;
	static int[] value = new int[100];
	static int[] bestValue = new int[100];
	static int[] bestExchangeValue = new int[101];
	static int[] bestDenom = new int[5];
	static int[] bestExchangeDenom = new int[6];

	public static void main(String[] args)
	{
		long startTime = System.nanoTime();
		totalChangeNumber();
		long estimatedTime = System.nanoTime() - startTime;
		System.out.println("Runtime: " + (double)estimatedTime/1000000000 + " seconds");
	}

	public static void genExactTable(int[] denom)
	{
		int totalScore = 0;
		for(int i = 1; i < value.length; i++)
		{
			value[i] = 999999;
		}
		for(int i = 0; i < denom.length; i++)
		{
			value[denom[i]] = 1;
			if (denom[i]%5 == 0)
			{
				totalScore+= N;
			}
			else
			{
				totalScore++;
			}
		}
		for(int i = 1; i < value.length; i++)
		{
			if(value[i] == 999999 && i > denom[0])
			{
				int mini = value[i-(denom[0])];
				for(int j = 0; j < denom.length; j++)
				{
					if (i > denom[j])
					{
						mini = Math.min(mini, value[i-denom[j]]);
					}
				}
				value[i] = 1 + mini;
				if(i%5 == 0)
				{
					totalScore += N * value[i];
				}
				else
				{
					totalScore += value[i];
				}
			}
			if(totalScore > score)
			{
				break;
			}
		}
		if(totalScore <= score && getScore(value) < score)
		{
			score = totalScore;
			bestDenom = denom;
			bestValue = value;
			System.out.print("{ " + bestDenom[0] + ", " + bestDenom[1] + ", " + bestDenom[2] + ", " + bestDenom[3] + ", " + bestDenom[4] + "}		");
			System.out.println("New Best Exact score: " + score + "  Average is: " + (float)score/(80+N*19));
		}
	}

	public static void genExchangeTable(int[] exchangeDenom)
	{
		int totalExchangeScore = 0;
		int[] exchangeValue = new int[101];
		for(int i = 1; i < exchangeValue.length; i++)
		{
			exchangeValue[i] = 999999;
		}
		for(int i = 0; i < exchangeDenom.length - 1; i++)
		{
			exchangeValue[exchangeDenom[i]] = 1;
			if (exchangeDenom[i]%5 == 0)
			{
				totalExchangeScore+= N;
			}
			else
			{
				totalExchangeScore++;
			}
		}
		exchangeValue[100] = 0;
		for(int i = 1; i < 100; i++)
		{
			if(exchangeValue[i] == 999999)
			{
				int mini = value[100-i];
				for(int j = 0; j < exchangeDenom.length - 1; j++)
				{
					if(exchangeDenom[j] > i)
					{
						mini = Math.min(mini, exchangeValue[exchangeDenom[j] - i] + 1);
					}
					else
					{
						mini = Math.min(mini, exchangeValue[i - exchangeDenom[j]] + 1);
					}
				}
				exchangeValue[i] = mini;
				if(i%5 == 0)
				{
					totalExchangeScore += N * exchangeValue[i];
				}
				else
				{
					totalExchangeScore += exchangeValue[i];
				}
			}
			if(totalExchangeScore > exchangeScore)
			{
				break;
			}
		}
		//secondary loop to quickly confirm values since previous loop is from 1-100 instead of 1-200.
		totalExchangeScore = 0;
		for(int i = 1; i < 100; i++)
		{
			if(exchangeValue[i] != exchangeValue[100-i]) {
				exchangeValue[i] = Math.min(exchangeValue[i], exchangeValue[100-i]);
				exchangeValue[100 - i] = Math.min(exchangeValue[i], exchangeValue[100-i]);
			}
			if(i%5 == 0)
				{
					totalExchangeScore += N * exchangeValue[i];
				}
				else
				{
					totalExchangeScore += exchangeValue[i];
				}
		}
		if(totalExchangeScore <= exchangeScore && getScore(exchangeValue) <= exchangeScore)
		{
			exchangeScore = totalExchangeScore;
			bestExchangeDenom = exchangeDenom;
			bestExchangeValue = exchangeValue;
			System.out.print("{ " + bestExchangeDenom[0] + ", " + bestExchangeDenom[1] + ", " + bestExchangeDenom[2] + ", " + bestExchangeDenom[3] + ", " + bestExchangeDenom[4] + "}		");
			System.out.println("New Best Exchange score: " + exchangeScore + "  Average is: " + (float)exchangeScore/(80+N*19));
		}
	}

	public static void totalChangeNumber()
	{
		int[] testDenom = new int[5];
		int[] testExchangetestDenom = new int[6];
		testDenom[0] = 1;
		testExchangetestDenom[5] = 100;
		//only looks at {i,j,k,l,m,n} denominations where i < j < k < l < m
		for(int i = 1; i < 100; i++)
		{
			testDenom[1] = i;
			testExchangetestDenom[0] = i;
			for(int j = 3; j < 100; j++)
			{
				if(j > i)
				{
					testDenom[2] = j;
					testExchangetestDenom[1] = j;
				}
				else
				{
					continue;
				}
				for(int k = 4; k < 100; k++)
				{
					if(k > j)
					{
						testDenom[3] = k;
						testExchangetestDenom[2] = k;
					}
					else
					{
						continue;
					}
					for(int m = 5; m < 100; m++)
					{
						if(m > k)
						{
							testDenom[4] = m;
							testExchangetestDenom[3] = m;
						}
						else
						{
							continue;
						}
						genExactTable(testDenom);
						for(int o = 1; o < 51; o++)
						{
							if(o > m)
							{
								testExchangetestDenom[4] = o;
								int[] temp = {i, j, k, m, o};
								genExactTable(temp);
								genExchangeTable(testExchangetestDenom);
							}
							else
							{
								continue;
							}
						}
					}
				}
			}
		}
	}

	public static int getScore(int[] array)
	{
		int temp = 0;
		for(int i = 1; i < 100; i++)
		{
			if(i%5 == 0)
			{
				temp += N * array[i];
			}
			else
			{
				temp += array[i];
			}
		}
		return temp;
	}
}