

/*
 * This is free and unencumbered software released into the public domain.
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org/>
 */

package mdfs.utils;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;



/**
 * Top-Down Splay Tree implementation (http://en.wikipedia.org/wiki/Splay_tree).
 * Some modification is added by Rasmus Holm, Implementing a separated Comparable key to index elements
 * 
 * @author Pedro Oliveira (http://www.cpdomina.net), original by Danny Sleator (sleator@cs.cmu.edu)
 * @author Modifyed by Rasmus Holm
 *
 * @param <Key>
 */
public class SplayTree<Key extends Comparable<Key>, E> implements Iterable<E> {

	private BinaryNode root;
	private final BinaryNode aux;
    private int size = 0;

	/**
	 * Build an empty Splay Tree
	 */
	public SplayTree() {
		root = null;
		aux = new BinaryNode(null, null);
	}


	/**
	 * Build an empty Splay Tree
	 * @return
	 */
	public static <T extends Comparable<T>, E> SplayTree<T, E> create() {
		return new SplayTree<T, E>();
	}

	/**
	 * Insert the given element into the tree.
	 * @param key the key for indexing
	 * @param element The element to insert
	 * @return
	 */
	public boolean insert(Key key, E element) {
		if (root == null) {
			root = new BinaryNode(key, element);
            size++;
			return true;
		}
		splay(key);

		final int c = key.compareTo(root.key);
		if (c == 0) {
			return false;
		}

		BinaryNode n = new BinaryNode(key, element);
		if (c < 0) {
			n.left = root.left;
			n.right = root;
			root.left = null;
		} else {
			n.right = root.right;
			n.left = root;
			root.right = null;
		}
		root = n;
        size++;
		return true;
	}
	/**
	 * Insert the given element into the tree.
	 * @param key to index element
	 * @param element The element to insert
	 * @return False if element already present, true otherwise
	 */
	public boolean put(Key key, E element) {
		return insert(key, element);
	}

	/**
	 * Remove the given element from the tree.
	 * @param key is the index to element to be removed.
	 * @return the element removed, null if dont exists.
	 */
	public E remove(Key key) {
		splay(key);
		
		if (key.compareTo(root.key) != 0) {
			return null;
		}
		
		E node = root.element;
		// Now delete the root
		if (root.left == null) {
			root = root.right;
		} else {
			BinaryNode x = root.right;
			root = root.left;
			splay(key);
			root.right = x;
		}
        size--;
		return node;
	}
	

	/**
	 * Find the smallest element in the tree.
	 * @return
	 */
	public E findMin() {
		BinaryNode x = root;
		if(root == null) return null;
		while(x.left != null) x = x.left;
		splay(x.key);
		return x.element;
	}

	/**
	 * Find the largest element in the tree.
	 * @return
	 */
	public E findMax() {
		BinaryNode x = root;
		if(root == null) return null;
		while(x.right != null) x = x.right;
		splay(x.key);
		return x.element;
	}

	/**
	 * Find an item in the tree.
	 * @param key The index of the element to find
	 * @return the 
	 */
	public E find(Key key) {
		if (root == null) return null;
		splay(key);
		if(root.key.compareTo(key) != 0) return null;
		return root.element;
	}

	/**
	 * Gets an item in the tree.
	 * @param key The index of the element to find
	 * @return
	 */
	public E get(Key key) {
		return find(key);
	}
	
	/**
	 * Check if the tree contains the given key.
	 * @param key
	 * @return True if present, false otherwise
	 */
	public boolean contains(Key key) {
		return find(key) != null;
	}

	/**
	 * Check if the tree contains the given key.
	 * @param key
	 * @return True if present, false otherwise
	 */
	public boolean containsKey(Key key) {
		return contains(key);
	}

	
	/**
	 * Test if the tree is logically empty.
	 * @return True if empty, false otherwise.
	 */
	public boolean isEmpty() {
		return root == null;
	}

    public int size(){
        return size;
    }
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<E> iterator() {
		return new SplayTreeIterator();
	}

	/**
	 * Internal method to perform a top-down splay.
	 * If the element is in the tree, then the {@link BinaryNode} containing that element becomes the root. 
	 * Otherwise, the root will be the ceiling or floor {@link BinaryNode} of the given element.
	 * @param element
	 */
	private void splay(Key element) {
		BinaryNode l, r, t, y;
		l = r = aux;
		t = root;
		aux.left = aux.right = null;
		while(true) {
			final int comp = element.compareTo(t.key);
			if (comp < 0) {
				if (t.left == null) break;
				if (element.compareTo(t.left.key) < 0) {
					y = t.left;                            /* rotate right */
					t.left = y.right;
					y.right = t;
					t = y;
					if (t.left == null) break;
				}
				r.left = t;                                 /* link right */
				r = t;
				t = t.left;
			} else if (comp > 0) {
				if (t.right == null) break;
				if (element.compareTo(t.right.key) > 0) {
					y = t.right;                            /* rotate left */
					t.right = y.left;
					y.left = t;
					t = y;
					if (t.right == null) break;
				}
				l.right = t;                                /* link left */
				l = t;
				t = t.right;
			} else {
				break;
			}
		}
		l.right = t.left;                                   /* assemble */
		r.left = t.right;
		t.left = aux.right;
		t.right = aux.left;
		root = t;
	}


	/**
	 * {@link SplayTree} internal node
	 * 
	 * @author Pedro Oliveira
	 *
	 */
	private class BinaryNode {

		public final Key key;          // Index/key of the node
		public E element;
		public BinaryNode left;         // Left child
		public BinaryNode right;        // Right child

		public BinaryNode(Key key, E element) {
			this.key = key;
			this.element = element;
			left = right = null;
		}
	}

	/**
	 * Stack-based {@link SplayTree} iterator
	 * @author Pedro Oliveira
	 *
	 */
	private class SplayTreeIterator implements Iterator<E> {

		private final Stack<BinaryNode> nodes;

		public SplayTreeIterator() {
			nodes = new Stack<BinaryNode>();
			pushLeft(root);
		}

		public boolean hasNext() {
			return !nodes.isEmpty();
		}

		public E next() {
			BinaryNode node = nodes.pop();
			if(node != null) {
				pushLeft(node.right);
				return node.element;
			}
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		private void pushLeft(BinaryNode node) {
			while (node != null) {
				nodes.push(node);
				node = node.left;
			}
		}

	}

}