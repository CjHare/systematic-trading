/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 *
 * * Neither the name of [project] nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.simulation.collections;

import java.util.LinkedList;

/**
 * Queue that keep the size to a limit.
 * 
 * @author CJ Hare
 */
public class LimitedQueue<E> extends LinkedList<E> {
	private static final long serialVersionUID = 1L;

	/** Maximum size of the queue. */
	private final int limit;

	/** Array to use when the limit is not yet reached. */
	private final E[] empty;

	/** Array re-used when invoking toArray . */
	private final E[] items;

	@SuppressWarnings("unchecked")
	public LimitedQueue( final Class<E> clazz, final int limit ) {
		this.limit = limit;

		// Occur the reflection cost here once
		this.items = (E[]) java.lang.reflect.Array.newInstance( clazz, limit );

		this.empty = (E[]) java.lang.reflect.Array.newInstance( clazz, 0 );

	}

	@Override
	public boolean add( final E o ) {
		super.add( o );

		while (size() > limit) {
			super.remove();
		}

		return true;
	}

	@Override
	/**
	 * More efficient implementation, with regard to memory usage.
	 */
	public E[] toArray() {
		if (size() >= limit) {
			return super.toArray( items );
		} else {
			return super.toArray( empty );
		}
	}

	@Override
	public <T> T[] toArray( final T[] a ) {
		throw new UnsupportedOperationException( "Please use LimitedQueue.toArray() instead" );
	}

	public int getLimit() {
		return limit;
	}
}
