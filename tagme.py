from typing import List
from py4j.java_gateway import JavaGateway
from fastapi import FastAPI
from pydantic import BaseModel


class ManyInput(BaseModel):
    texts: List[str]


class OneInput(BaseModel):
    text: str

gateway = JavaGateway()
app = FastAPI()


def _tag_many(texts: List[str]) -> List[List[str]]:
    jvm_texts = gateway.jvm.java.util.ArrayList()
    for t in texts:
        jvm_texts.append(t)
    tags = gateway.entry_point.annotateMany(jvm_texts)
    all_mentions = []
    for i, mention in enumerate(tags):
        annotations = mention.getAnnotations()
        spans = [(s.getStart(), s.getEnd()) for s in mention.getSpans()]
        text = mention.getText()
        o_text = texts[i]
        annotations = [(
            gateway.entry_point.idToPage(a.getTopic()),
            a.getStart(), a.getEnd(),
            a.getRho()
        ) for a in annotations]
        mentions = []
        for (o_start, o_end), (topic, start, end, rho)  in zip(spans, annotations):
            if rho > .1:
                mentions.append({'page': topic, 'start': o_start, 'end': o_end, 'rho': rho})
        all_mentions.append(mentions)
    return all_mentions


@app.get('/')
def root():
    return {'message': 'hello'}


@app.post('/tag/many')
def tag_many(many_input: ManyInput):
    return _tag_many(many_input.texts)


@app.post('/tag/one')
def tag_one(one_input: OneInput):
    pass
