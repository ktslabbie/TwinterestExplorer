package jp.titech.twitter.ontology.dbpedia;

public class DBpediaResourceOccurrence {
	
	private DBpediaResource resource;
	private String id = "", surfaceForm, context;
	private int textOffset;
	private double similarityScore = -1, percentageOfSecondRank = -1, contextualScore = -1;


	public DBpediaResourceOccurrence(String id, DBpediaResource resource, String surfaceForm, String context, int textOffset, 
										double similarityScore, double percentageOfSecondRank, double contextualScore) {
		this.resource = resource;
		this.id = id;
		this.surfaceForm = surfaceForm;
		this.context = context;
		this.textOffset = textOffset;
		this.similarityScore = similarityScore;
		this.percentageOfSecondRank = percentageOfSecondRank;
		this.contextualScore = contextualScore;
	}
	
	public DBpediaResourceOccurrence(DBpediaResource resource, String surfaceForm, String context, int textOffset) {
		this.resource = resource;
		this.surfaceForm = surfaceForm;
		this.context = context;
		this.textOffset = textOffset;
	}
	
	@Override
	public String toString() {
		int span = 50;
		int start = (this.textOffset < 0 || this.textOffset < span) ? 0 : this.textOffset - span;
		int end = (this.textOffset + span > this.context.length()) ? this.context.length() : this.textOffset + span;
		String text = (start > end) ? "Text[]" : "Text[... " + context.substring(start, end) + " ...]";
		String score = (similarityScore == -1.0) ? "" : String.format("%.3f", similarityScore);
		if (!this.id.isEmpty())
			return this.id + ": ";
		else return "" + this.surfaceForm + " -" + score + "-> " + this.resource + " - at position *" + this.textOffset + "* in - " + text;
	}

	/**
	 * @return the resource
	 */
	public DBpediaResource getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(DBpediaResource resource) {
		this.resource = resource;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the surfaceForm
	 */
	public String getSurfaceForm() {
		return surfaceForm;
	}

	/**
	 * @param surfaceForm the surfaceForm to set
	 */
	public void setSurfaceForm(String surfaceForm) {
		this.surfaceForm = surfaceForm;
	}

	/**
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(String context) {
		this.context = context;
	}

	/**
	 * @return the textOffset
	 */
	public int getTextOffset() {
		return textOffset;
	}

	/**
	 * @param textOffset the textOffset to set
	 */
	public void setTextOffset(int textOffset) {
		this.textOffset = textOffset;
	}

	/**
	 * @return the similarityScore
	 */
	public double getSimilarityScore() {
		return similarityScore;
	}

	/**
	 * @param similarityScore the similarityScore to set
	 */
	public void setSimilarityScore(double similarityScore) {
		this.similarityScore = similarityScore;
	}

	/**
	 * @return the percentageOfSecondRank
	 */
	public double getPercentageOfSecondRank() {
		return percentageOfSecondRank;
	}

	/**
	 * @param percentageOfSecondRank the percentageOfSecondRank to set
	 */
	public void setPercentageOfSecondRank(double percentageOfSecondRank) {
		this.percentageOfSecondRank = percentageOfSecondRank;
	}

	/**
	 * @return the contextualScore
	 */
	public double getContextualScore() {
		return contextualScore;
	}

	/**
	 * @param contextualScore the contextualScore to set
	 */
	public void setContextualScore(double contextualScore) {
		this.contextualScore = contextualScore;
	}
	
	
}



/*def compareTo(that : DBpediaResourceOccurrence) : Int = {
val c = this.similarityScore.compare(that.similarityScore)
val str1 : String = this.id+this.resource.uri+this.surfaceForm.name+this.textOffset.toString+this.context.text
val str2 : String = that.id+that.resource.uri+that.surfaceForm.name+that.textOffset.toString+that.context.text
if (c==0) str1.compare(str2) else c
}*/

/*
override def equals(obj : Any) : Boolean = {
obj match {
case that: DBpediaResourceOccurrence =>
(  resource.equals(that.resource)
&& surfaceForm.equals(that.surfaceForm)
&& context.equals(that.context)
//&& (textOffset == that.textOffset )
)
case _ => false;
}
}*/

