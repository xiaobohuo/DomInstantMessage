package com.dom.ination.domforandroid.network.biz;

public interface IResult {

	/**
	 * 缓存过期
	 * 
	 * @return
	 */
	boolean outofdate();
	
	/**
	 * 是否是缓存数据
	 * 
	 * @return
	 */
	boolean fromCache();
	
	/**
	 * 没有更多数据了
	 * 
	 * @return
	 */
	boolean endPaging();

	/**
	 * 页码信息
	 *
	 * @return
	 */
	String[] pagingIndex();

}
