package com.moonshile.helper;

public class MoonshileSort {
	
	/**
	 * @param <T> a list type, such as List&lt;String&gt;
	 * @param <F> a type of the list items, such as String
	 */
	public interface MoonshileList<T, F>{
		public F get(T list, int i);
		public void set(T list, F f, int i);
	}
	
	public interface Compare<F>{
		public int cmp(F f1, F f2);
	}

	/********************************** Constructor ********************************************/

	private MoonshileSort(){}
	
	/********************************** Methods ********************************************/

	/**
	 * sort a list with merge-sort, both start and end will be sorted
	 * @param list the list
	 * @param start start index of the part to be sorted 
	 * @param end end index of the part to be sorted
	 * @param cmp the compare method
	 */
	public static <T, F> void mergeSort(MoonshileList<T, F> listMethod, T list, int start, int end, Compare<F> cmp){
		if(start >= end){
			return;
		}
		int middle = (start + end)/2;
		mergeSort(listMethod, list, start, middle, cmp);
		mergeSort(listMethod, list, middle + 1, end, cmp);
		merge(listMethod, list, start, middle, end, cmp);
	}
	
	private static <T, F> void merge(MoonshileList<T, F> listMethod, T list, int start, int middle, int end, Compare<F> cmp){
		int i = start, j = middle + 1;
		while(i < j && j <= end){
			// first i greater than j
			while(i < j && cmp.cmp(listMethod.get(list, i), listMethod.get(list, j)) <= 0){
				i++;
			}
			int old_j = j;
			// first j not lower than i
			while(j <= end && cmp.cmp(listMethod.get(list, j), listMethod.get(list, i)) < 0){
				j++;
			}
			shift(listMethod, list, i, old_j - 1, j - 1);
			i += j - old_j;
		}
	}
	
	private static <T, F> void shift(MoonshileList<T, F> listMethod, T list, int start, int middle, int end){
		reverse(listMethod, list, start, middle);
		reverse(listMethod, list, middle + 1, end);
		reverse(listMethod, list, start, end);
	}
	
	private static <T, F> void reverse(MoonshileList<T, F> listMethod, T list, int start, int end){
		while(end > start){
			F tmp = listMethod.get(list, start);
			listMethod.set(list, listMethod.get(list, end), start);
			listMethod.set(list, tmp, end);
			start++;
			end--;
		}
	}
	
	/********************************** Fields ********************************************/

}
