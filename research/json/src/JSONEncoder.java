public class JSONEncoder implements JXVisitor {
		private Writer writer;

		public JSONEncoder() {
		}

		public void encode(JXElement anElement, Writer aStringBuffer) throws IOException {
			writer = aStringBuffer;
			output("<pre>{");
			new JXWalker(this).traverse(anElement);
			output("</pre>}");
		}

		public void visitElementPre(JXElement element)   {
			if (hasSimilarSiblings(element)) {
				if (isFirstChild(element)) {
					output(element.getTag());
					output(": ");

					output("\n[\n");
				}
			} else {
				output(element.getTag());
				output(": ");
			}
			if (element.hasChildren() || hasAttrs(element)) {
				output("{");
			}

			if (hasAttrs(element)) {
				visitAttrs(element);
			}
		}

		public void visitElementPost(JXElement element)  {
			if (hasSimilarSiblings(element)) {
				if (isLastChild(element)) {
					output("]\n");
				} else {
					output(",\n");
				}
			} else {
				if (!element.hasChildren() && !hasAttrs(element) && isLastChild(element)) {
					output("}\n");
				}
				if (!isLastChild(element) && element.getParent() != null) {
					output(",Y\n");
				}
			}
		}

		public void visitCDATA(byte[] theCDATA) {
			//output("<![CDATA[");
			outputString(new String(theCDATA));
			// output("]]>");
		}

		public void visitText(String text)  {
			outputString(text);
		}


		private boolean hasAttrs(JXElement element)   {
			return element.getAttrs() != null &&  element.getAttrs().size() > 0;
		}


		private boolean hasSiblings(JXElement element)   {
			return element.getParent() != null &&  element.getParent().getChildCount() > 1;
		}

		private boolean isFirstChild(JXElement element)   {
				return hasSiblings(element) && element.getParent().getChildren().get(0) == element;
		}

		private boolean isLastChild(JXElement element)   {
				return hasSiblings(element) && element.getParent().getChildren().get(element.getParent().getChildCount()-1) == element;
		}

		private boolean hasSimilarSiblings(JXElement element)   {
				return hasSiblings(element) && element.getParent().getChildrenByTag(element.getTag()).size() > 1;
		}

		private void visitAttrs(JXElement element)   {
			JXAttributeTable attrs = element.getAttrs();
			Iterator iter = attrs.keys();
			String n,v;
			while (iter.hasNext()) {
				n = (String) iter.next();
				v = attrs.get(n);
				output(n);
				output(": ");
				outputString(v);
				if (iter.hasNext()) {
					output(",");
				}
			}
			if (!isLastChild(element)) {
				output(",\n");
			}
		}
		private void output(String s) {
			try {
				writer.write(s);
			} catch (IOException ioe) {
				System.out.println("error: " + ioe);
			}
		}

		private void outputString(String s) {
			try {
				writer.write("'" + s + "'");
			} catch (IOException ioe) {
				System.out.println("error: " + ioe);
			}
		}
	}

