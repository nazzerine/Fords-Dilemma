import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;

public class Solution {

  public static void main(String[] args) {
      
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			TwoThreeTree tree = new TwoThreeTree();
			try {
                //read in the database size
				int databaseSz = Integer.parseInt(in.readLine());
                //read in each line and insert into tree
			    for (int i = 0; i < databaseSz; i++) {
                    String[] arr = in.readLine().split(" ");
                    twothree.insert(arr[0], Integer.parseInt(arr[1]), tree);
                 }
				
                //read in the number of queries
				int querySz = Integer.parseInt(in.readLine());
                //read in each query and properly search and print what the query wants
				for (int i = 0; i < querySz; i ++) {
					String[] arr = in.readLine().split(" ");
					String x = arr[0];
					String y = arr[1];
					if (x.compareTo(y) < 0) {
						twothree.printRange(x, y, tree.root, tree.height);
					} else {
                        //if the query x and y are out of order
						twothree.printRange(y,  x, tree.root, tree.height);
					}
				}
			} catch (IOException e) {
				System.err.println("IO Exception");
			}
			//close BufferReader
			in.close();
			
		} catch (FileNotFoundException e) {
			System.err.println("Error: File not found.");
		} catch (IOException e) {
			System.err.println("IO Exception");
		}

		
	}
	
}
	
class twothree {

   static void insert(String key, int value, TwoThreeTree tree) {
   // insert a key value pair into tree (overwrite existsing value
   // if key is already present)

      int h = tree.height;

      if (h == -1) {
          LeafNode newLeaf = new LeafNode();
          newLeaf.guide = key;
          newLeaf.value = value;
          tree.root = newLeaf; 
          tree.height = 0;
      }
      else {
         WorkSpace ws = doInsert(key, value, tree.root, h);

         if (ws != null && ws.newNode != null) {
         // create a new root

            InternalNode newRoot = new InternalNode();
            if (ws.offset == 0) {
               newRoot.child0 = ws.newNode; 
               newRoot.child1 = tree.root;
            }
            else {
               newRoot.child0 = tree.root; 
               newRoot.child1 = ws.newNode;
            }
            resetGuide(newRoot);
            tree.root = newRoot;
            tree.height = h+1;
         }
      }
   }

   static WorkSpace doInsert(String key, int value, Node p, int h) {
   // auxiliary recursive routine for insert

      if (h == 0) {
         // we're at the leaf level, so compare and 
         // either update value or insert new leaf

         LeafNode leaf = (LeafNode) p; //downcast
         int cmp = key.compareTo(leaf.guide);

         if (cmp == 0) {
            leaf.value = value; 
            return null;
         }

         // create new leaf node and insert into tree
         LeafNode newLeaf = new LeafNode();
         newLeaf.guide = key; 
         newLeaf.value = value;

         int offset = (cmp < 0) ? 0 : 1;
         // offset == 0 => newLeaf inserted as left sibling
         // offset == 1 => newLeaf inserted as right sibling

         WorkSpace ws = new WorkSpace();
         ws.newNode = newLeaf;
         ws.offset = offset;
         ws.scratch = new Node[4];

         return ws;
      }
      else {
         InternalNode q = (InternalNode) p; // downcast
         int pos;
         WorkSpace ws;

         if (key.compareTo(q.child0.guide) <= 0) {
            pos = 0; 
            ws = doInsert(key, value, q.child0, h-1);
         }
         else if (key.compareTo(q.child1.guide) <= 0 || q.child2 == null) {
            pos = 1;
            ws = doInsert(key, value, q.child1, h-1);
         }
         else {
            pos = 2; 
            ws = doInsert(key, value, q.child2, h-1);
         }

         if (ws != null) {
            if (ws.newNode != null) {
               // make ws.newNode child # pos + ws.offset of q

               int sz = copyOutChildren(q, ws.scratch);
               insertNode(ws.scratch, ws.newNode, sz, pos + ws.offset);
               if (sz == 2) {
                  ws.newNode = null;
                  ws.guideChanged = resetChildren(q, ws.scratch, 0, 3);
               }
               else {
                  ws.newNode = new InternalNode();
                  ws.offset = 1;
                  resetChildren(q, ws.scratch, 0, 2);
                  resetChildren((InternalNode) ws.newNode, ws.scratch, 2, 2);
               }
            }
            else if (ws.guideChanged) {
               ws.guideChanged = resetGuide(q);
            }
         }

         return ws;
      }
   }


   static int copyOutChildren(InternalNode q, Node[] x) {
   // copy children of q into x, and return # of children

      int sz = 2;
      x[0] = q.child0; x[1] = q.child1;
      if (q.child2 != null) {
         x[2] = q.child2; 
         sz = 3;
      }
      return sz;
   }

   static void insertNode(Node[] x, Node p, int sz, int pos) {
   // insert p in x[0..sz) at position pos,
   // moving existing extries to the right

      for (int i = sz; i > pos; i--)
         x[i] = x[i-1];

      x[pos] = p;
   }

   static boolean resetGuide(InternalNode q) {
   // reset q.guide, and return true if it changes.

      String oldGuide = q.guide;
      if (q.child2 != null)
         q.guide = q.child2.guide;
      else
         q.guide = q.child1.guide;

      return q.guide != oldGuide;
   }


   static boolean resetChildren(InternalNode q, Node[] x, int pos, int sz) {
   // reset q's children to x[pos..pos+sz), where sz is 2 or 3.
   // also resets guide, and returns the result of that

      q.child0 = x[pos]; 
      q.child1 = x[pos+1];

      if (sz == 3) 
         q.child2 = x[pos+2];
      else
         q.child2 = null;

      return resetGuide(q);
   }
   
   static int search(String x, Node p, int height) {
	  if (height > 0) {
		  InternalNode test = (InternalNode) p;
		  if (x.compareTo(test.child0.guide) <= 0) {
			  return search(x,test.child0, height-1);
		  } else if (x.compareTo(test.child1.guide) <= 0 || test.child2 == null) {
			  return search(x, test.child1, height-1);
		  } else {
			  return search(x, test.child2, height-1);
		  }
	  } else { //if height is 0 and node is a leaf
		  LeafNode leafTest = (LeafNode) p;
		  if (x == leafTest.guide) {
			  return leafTest.value;
		  } else {
			  return -1; //this means that x is not in the tree
		  }
	  }
   }
   
    static void printRange (String x, String y, Node node, int height) {
       //base case
       if (node == null) {
           return;
       }
        
       if (height < 1) {
           LeafNode found = (LeafNode) node;
           if (x.compareTo(found.guide) <= 0 && y.compareTo(found.guide) >= 0) { 
               //found the leaf to print
               System.out.println(found.guide +" "+ found.value);
           }
       } else {
           InternalNode n = (InternalNode) node;

           if (x.compareTo(n.child0.guide) <= 0) {
               //search through child0's subtree
               printRange(x, y, n.child0, height-1);
           }
           //search through child1's subtree
           if (x.compareTo(n.child1.guide) <= 0 && y.compareTo(n.child0.guide) >= 0 || 
               y.compareTo(n.child1.guide) >= 0) {
               printRange(x, y, n.child1, height-1);
           }


           if (n.child2 != null) {
               //if child2 exists, search through child2's subtree
               if (x.compareTo(n.child1.guide) <= 0 && y.compareTo(n.child0.guide) >= 0 ||
                   y.compareTo(n.child1.guide) >= 0) {
                   printRange(x, y, n.child2, height-1);
               }
           }
           
       }   
    }
}

class Node {
    String guide;
    // guide points to max key in subtree rooted at node
}

class InternalNode extends Node {
    Node child0, child1, child2;
    // child0 and child1 are always non-null
    // child2 is null iff node has only 2 children
}

class LeafNode extends Node {
    // guide points to the key

    int value;
}

class TwoThreeTree {
    Node root;
    int height;

    TwoThreeTree() {
        root = null;
        height = -1;
    }
}

class WorkSpace {
    // this class is used to hold return values for the recursive doInsert
    // routine

    Node newNode;
    int offset;
    boolean guideChanged;
    Node[] scratch;
}