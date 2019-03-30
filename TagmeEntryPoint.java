import java.io.IOException;
import java.util.List;
import py4j.GatewayServer;
import java.util.ArrayList;

import it.acubelab.tagme.AnnotatedText;
import it.acubelab.tagme.Annotation;
import it.acubelab.tagme.Disambiguator;
import it.acubelab.tagme.RelatednessMeasure;
import it.acubelab.tagme.RhoMeasure;
import it.acubelab.tagme.Segmentation;
import it.acubelab.tagme.TagmeParser;
import it.acubelab.tagme.config.TagmeConfig;
import it.acubelab.tagme.config.Config.IndexType;
import it.acubelab.tagme.preprocessing.DatasetLoader;
import it.acubelab.tagme.preprocessing.TopicSearcher;
import it.acubelab.tagme.preprocessing.graphs.InGraphArray;
import it.acubelab.tagme.preprocessing.graphs.WikiGraphs;


public class TagmeEntryPoint {
  public String lang = "en";
  public RelatednessMeasure relatedness;
  public TagmeParser parser;
  public Disambiguator disamb;
  public Segmentation segmentation;
  public RhoMeasure rho;
  public TopicSearcher searcher;
  public TagmeEntryPoint() throws IOException {
		TagmeConfig.init();
    relatedness = RelatednessMeasure.create(lang);
    parser = new TagmeParser(lang, true);
    disamb = new Disambiguator(lang);
    segmentation = new Segmentation();
    rho = new RhoMeasure();
    searcher = new TopicSearcher(lang);
  }

  public class Mention {
    public String text;
    public List<Annotation> annotations;
    public List<Span> spans;
    public Mention(String text, List<Annotation> annotations, List<Span> spans) {
      this.text = text;
      this.annotations = annotations;
      this.spans = spans;
    }

    public String getText() {
      return this.text;
    }

    public List<Annotation> getAnnotations() {
      return this.annotations;
    }

    public List<Span> getSpans() {
      return this.spans;
    }
  }

  public String idToPage(int wikiPageId) throws IOException {
    return searcher.getTitle(wikiPageId);
  }

  public class Span {
    public int start;
    public int end;
    public Span(int start, int end) {
      this.start = start;
      this.end = end;
    }
    public int getStart() {
      return this.start;
    }
    public int getEnd() {
      return this.end;
    }
  }

  public Mention annotate(String text) {
		AnnotatedText ann_text = new AnnotatedText(text);
		parser.parse(ann_text);
		segmentation.segment(ann_text);
		disamb.disambiguate(ann_text, relatedness);
		rho.calc(ann_text, relatedness);
		List<Annotation> annots = ann_text.getAnnotations();
    ArrayList<Span> spans = new ArrayList<Span>();
    for (Annotation ann : annots) {
      spans.add(new Span(ann_text.getOriginalTextStart(ann), ann_text.getOriginalTextEnd(ann)));
    }
    Mention mention = new Mention(ann_text.getText(), annots, spans);
    return mention;
  }

  public List<Mention> annotateMany(List<String> textList) {
    ArrayList<Mention> mentions = new ArrayList<Mention>();
    for (String text : textList) {
      mentions.add(this.annotate(text));
    }
    return mentions;
  }

	public static void main(String[] args) throws IOException {
    TagmeEntryPoint app = new TagmeEntryPoint();
    GatewayServer server = new GatewayServer(app);
    server.start();
	}

}
