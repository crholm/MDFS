package mdfs.utils.tests.junit;

import static org.junit.Assert.*;

import mdfs.utils.SplayTree;

import org.junit.Test;

public class SplayTreeTest {

	@Test
	public void testSplayTree() {
		SplayTree<Integer, String> tree = new SplayTree<Integer, String>();
		assertNotNull(tree);
	}

	@Test
	public void testInsert() {
		SplayTree<Integer, Integer> tree = new SplayTree<Integer, Integer>();
		
		for(int i = 0; i<100; i++){
			assertTrue(tree.insert(i, i));
		}
		for(int i = 0; i<100; i++){
			assertTrue(!tree.insert(i, i));
		}
	}

	@Test
	public void testPut() {
		testInsert();
	}

	@Test
	public void testRemove() {
		SplayTree<Integer, Integer> tree = new SplayTree<Integer, Integer>();
		
		for(int i = 0; i<100; i++){
			tree.insert(i, i);
		}
		
		for(int i = 25; i<75; i++){
			assertTrue(tree.remove(i) == i);
		}
		for(int i = 25; i<75; i++){
			assertTrue(tree.remove(i) == null);
		}
		
	}

	@Test
	public void testFindMin() {
		SplayTree<Integer, Integer> tree = new SplayTree<Integer, Integer>();
		for(int i = 0; i<100; i++){
			tree.insert(i, i);
		}
		
		assertTrue(tree.findMin() == 0);
	}

	@Test
	public void testFindMax() {
		SplayTree<Integer, Integer> tree = new SplayTree<Integer, Integer>();
		for(int i = 0; i<100; i++){
			tree.insert(i, i);
		}
		
		assertTrue(tree.findMax() == 99);
	}

	@Test
	public void testFind() {
		SplayTree<Integer, Integer> tree = new SplayTree<Integer, Integer>();
		for(int i = 0; i<100; i++){
			tree.insert(i, i);
		}
		
		for(int i = 0; i < 10; i++){
			int index = (int)(Math.random()*i);
			assertTrue(tree.find(index) == index);
		}
	}

	@Test
	public void testGet() {
		testFind();
	}

	@Test
	public void testContains() {
		SplayTree<Integer, Integer> tree = new SplayTree<Integer, Integer>();
		for(int i = 0; i<100; i++){
			tree.insert(i, i);
		}
		for(int i = 0; i < 10; i++){
			int index = (int)(Math.random()*i);
			assertTrue(tree.contains(index));
		}
		for(int i = 0; i < 10; i++){
			int index = (int)(Math.random()*i+100);
			assertTrue(!tree.contains(index));
		}
	}

	@Test
	public void testContainsKey() {
		testContains();
	}

	@Test
	public void testIsEmpty() {
		SplayTree<Integer, Integer> tree = new SplayTree<Integer, Integer>();
		assertTrue(tree.isEmpty());
		for(int i = 0; i<100; i++){
			tree.insert(i, i);
			assertTrue(!tree.isEmpty());
		}
	}

}
