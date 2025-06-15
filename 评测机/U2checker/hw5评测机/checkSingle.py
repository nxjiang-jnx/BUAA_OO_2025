import os
import subprocess

CACHE_PATH = "cache"
C_NAME = './main'  # 确保这是你的C语言可执行文件路径

# 假设这是你调用C语言函数来判断正确性的代码
def check_output_correctness(stdout_path):
    # 运行 C 语言程序并传递 stdout 文件路径作为参数
    checker_output = subprocess.run([C_NAME, stdout_path], capture_output=True, text=True).stdout.strip()
    
    # 输出 C 语言程序的判断结果
    print(f"Checker Output: {checker_output}")
    return checker_output


# 读取指定目录下的所有txt文件
def get_output_files(directory):
    txt_files = [f for f in os.listdir(directory) if f.endswith('.txt')]
    return txt_files


# 主函数
def main():
    directory = os.getcwd()  # 获取当前工作目录
    txt_files = get_output_files(directory)

    if not txt_files:
        print("No txt files found in the current directory.")
        return

    print("Processing the following files:")
    print(txt_files)

    for txt_file in txt_files:
        stdout_path = os.path.join(directory, txt_file)
        print(f"Processing {stdout_path}")
        
        result = check_output_correctness(stdout_path)
        
        if result != "Correct":
            print(f"Error: {result} for {stdout_path}")
        else:
            print(f"Output for {stdout_path} is Correct!")


if __name__ == "__main__":
    main()
