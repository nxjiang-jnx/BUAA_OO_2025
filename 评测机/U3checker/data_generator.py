from random import choice, randint, random, sample

MAX_VALUE = 200
MAX_M_VALUE = 200
MAX_AGE = 200

# Updated instruction set with new abbreviations included
instrs = [
    'ap', 'ar', 'mr',
    #'at', 'dt', 'att', 'dft',
    #'qv', 'qci', 'qts', 'qtav', 'qba',
    'coa', 'doa', 'ca', 'da', 'foa',
    #'qsp', 'qbc', 'qra', 'qtvs', 'qcs',
    'am', 'aem', 'arem', 'afm', 'sm',
    'sei', 'dce', 'qsv', 'qrm', 'qp', 'qm'
]

def generate(a):
    return normal_data(50000, 10)

def person_instr(instr_name, id):
    return f"{instr_name} {id} MS-{id} {randint(0, MAX_AGE)}\n"

def relation_instr(instr_name, id1, id2, value):
    return f"{instr_name} {id1} {id2} {value}\n"

def tag_instr(instr_name, id1, tag_id):
    return f"{instr_name} {id1} {tag_id}\n"

def tag2_instr(instr_name, id1, id2, tag_id):
    return f"{instr_name} {id1} {id2} {tag_id}\n"

def check_instr(instr_name, id1, id2):
    return f"{instr_name} {id1} {id2}\n"

def check_one(instr_name, id1):
    return f"{instr_name} {id1}\n"

def check_one_with_tag(instr_name, id1, tag_id):
    return f"{instr_name} {id1} {tag_id}\n"

def overall_instr(instr_name):
    return f"{instr_name}\n"

# New instruction formats
def create_official_account(id1, acc_id):
    return f"coa {id1} {acc_id} ACC-{acc_id}\n"

def delete_official_account(id1, acc_id):
    return f"doa {id1} {acc_id}\n"

def contribute_article(id1, acc_id, art_id):
    return f"ca {id1} {acc_id} {art_id} ART-{art_id}\n"

def delete_article(id1, acc_id, art_id):
    return f"da {id1} {acc_id} {art_id}\n"

def follow_official_account(id1, acc_id):
    return f"foa {id1} {acc_id}\n"

def query_shortest_path(id1, id2):
    return f"qsp {id1} {id2}\n"

def query_best_contributor(acc_id):
    return f"qbc {acc_id}\n"

def query_received_articles(id1):
    return f"qra {id1}\n"

def query_tag_value_sum(id1, tag_id):
    return f"qtvs {id1} {tag_id}\n"

def query_couple_sum():
    return f"qcs\n"

def add_message(mid, sv, tp, id1, id2_or_tag):
    return f"am {mid} {sv} {tp} {id1} {id2_or_tag}\n"

def add_emoji_message(mid, eid, tp, id1, id2_or_tag):
    return f"aem {mid} {eid} {tp} {id1} {id2_or_tag}\n"

def add_red_envelope_message(mid, money, tp, id1, id2_or_tag):
    return f"arem {mid} {money} {tp} {id1} {id2_or_tag}\n"

def add_forward_message(mid, aid, tp, id1, id2_or_tag):
    return f"afm {mid} {aid} {tp} {id1} {id2_or_tag}\n"

def send_message(mid):
    return f"sm {mid}\n"

def store_emoji_id(eid):
    return f"sei {eid}\n"

def delete_cold_emoji(limit):
    return f"dce {limit}\n"

def query_social_value(pid):
    return f"qsv {pid}\n"

def query_received_messages(pid):
    return f"qrm {pid}\n"

def query_popularity(pid):
    return f"qp {pid}\n"

def query_money(pid):
    return f"qm {pid}\n"


def ln_data(people_num):
    sample_num = min(people_num, 100)
    sample_list = sample(range(1, people_num + 1), sample_num)
    instrlist = [f"ln {sample_num}\n"]
    instrlist.append(' '.join(map(str, sample_list)) + '\n')
    instrlist.append(' '.join(["MS-" + str(i) for i in sample_list]) + '\n')
    instrlist.append(' '.join([str(randint(0, MAX_AGE)) for _ in sample_list]) + '\n')
    for i in range(1, sample_num):
        values = [1 if random() < 0.99 else 0 for _ in range(i)]
        instrlist.append(' '.join(map(str, values)) + '\n')
    return instrlist

def normal_data(instr_num, people_num):
    instrlist = []
    tag_id_range = people_num // 2
    for i in range(randint(int(people_num * 0.7), people_num)):
        instrlist.append(person_instr('ap', i))
    for _ in range(instr_num - people_num):
        instr = choice(instrs)
        id1 = randint(1, people_num)
        id2 = randint(1, people_num)
        tag_id = randint(1, tag_id_range)
        acc_id = randint(1, people_num)
        art_id = randint(1, 500)
        if instr == 'ap':
            instrlist.append(person_instr('ap', id1))
        elif instr == 'ar':
            instrlist.append(relation_instr('ar', id1, id2, randint(1, MAX_VALUE)))
        elif instr == 'mr':
            instrlist.append(relation_instr('mr', id1, id2, randint(-MAX_M_VALUE, 0)))
        elif instr == 'at':
            instrlist.append(tag_instr('at', id1, tag_id))
        elif instr == 'dt':
            instrlist.append(tag_instr('dt', id1, tag_id))
        elif instr == 'att':
            instrlist.append(tag2_instr('att', id1, id2, tag_id))
        elif instr == 'dft':
            instrlist.append(tag2_instr('dft', id1, id2, tag_id))
        elif instr == 'qv':
            instrlist.append(check_instr('qv', id1, id2))
        elif instr == 'qci':
            instrlist.append(check_instr('qci', id1, id2))
        elif instr == 'qts':
            instrlist.append(overall_instr('qts'))
        elif instr == 'qtav':
            instrlist.append(check_one_with_tag('qtav', id1, tag_id))
        elif instr == 'qba':
            instrlist.append(check_one('qba', id1))
        elif instr == 'coa':
            instrlist.append(create_official_account(id1, acc_id))
        elif instr == 'doa':
            instrlist.append(delete_official_account(id1, acc_id))
        elif instr == 'ca':
            instrlist.append(contribute_article(id1, acc_id, art_id))
        elif instr == 'da':
            instrlist.append(delete_article(id1, acc_id, art_id))
        elif instr == 'foa':
            instrlist.append(follow_official_account(id1, acc_id))
        elif instr == 'qsp':
            instrlist.append(query_shortest_path(id1, id2))
        elif instr == 'qbc':
            instrlist.append(query_best_contributor(acc_id))
        elif instr == 'qra':
            instrlist.append(query_received_articles(id1))
        elif instr == 'qtvs':
            instrlist.append(query_tag_value_sum(id1, tag_id))
        elif instr == 'qcs':
            instrlist.append(query_couple_sum())
        elif instr == 'am':
            instrlist.append(add_message(randint(1, 10000), randint(1, 100), randint(0, 1), id1, id2 if random() < 0.5 else randint(1, tag_id_range)))
        elif instr == 'aem':
            instrlist.append(add_emoji_message(randint(1, 10000), randint(0, 999), randint(0, 1), id1, id2 if random() < 0.5 else randint(1, tag_id_range)))
        elif instr == 'arem':
            instrlist.append(add_red_envelope_message(randint(1, 10000), randint(1, 200), randint(0, 1), id1, id2 if random() < 0.5 else randint(1, tag_id_range)))
        elif instr == 'afm':
            instrlist.append(add_forward_message(randint(1, 10000), randint(1, 10000), randint(0, 1), id1, id2 if random() < 0.5 else randint(1, tag_id_range)))
        elif instr == 'sm':
            instrlist.append(send_message(randint(1, 10000)))
        elif instr == 'sei':
            instrlist.append(store_emoji_id(randint(0, 999)))
        elif instr == 'dce':
            instrlist.append(delete_cold_emoji(randint(1, 10)))
        elif instr == 'qsv':
            instrlist.append(query_social_value(id1))
        elif instr == 'qrm':
            instrlist.append(query_received_messages(id1))
        elif instr == 'qp':
            instrlist.append(query_popularity(id1))
        elif instr == 'qm':
            instrlist.append(query_money(id1))

    return instrlist

if __name__ == '__main__':
    # print(generate(300, 100))
    instrs = generate(True)
    for entry in instrs:
        print(entry, end="")