package com.zzy.dicegames.ui.game.liarsdice;

import com.zzy.dicegames.R;
import com.zzy.dicegames.ui.game.BaseGameViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * 大话骰(Liar's Dice)游戏ViewModel<br>
 * 2~4位玩家各摇5颗骰子，只能看到自己的骰子，按座位顺序轮流叫数，
 * 猜场上所有骰子中某个点数的总数。<br>
 * 叫数格式为"X个Y飞/斋"：飞（默认）表示1点当万能，斋表示1点不算万能，叫1点时默认斋。<br>
 * 轮到玩家时可以加码（更大的叫数）或开骰质疑上家。固定进行10局，
 * 每局输家饮酒并记一负，其余玩家记一胜，游戏结束时按获胜局数最多、输的局数最少排名。
 *
 * @author 赵正阳
 */
public class LiarsDiceGameViewModel extends BaseGameViewModel {
    /** 每个玩家的骰子个数 */
    public static final int NUM_DICE_PER_PLAYER = 5;

    /** 最小玩家数 */
    public static final int MIN_PLAYERS = 2;

    /** 最大玩家数 */
    public static final int MAX_PLAYERS = 4;

    /** 默认玩家数 */
    public static final int DEFAULT_PLAYERS = 2;

    /** 总局数 */
    public static final int TOTAL_ROUNDS = 10;

    /** 人类玩家编号 */
    public static final int PLAYER_HUMAN = 0;

    /** 计算机玩家操作的延迟(ms) */
    private static final int DELAY = 1000;

    /** 玩家数量 */
    private int numPlayers;

    /** 当前玩家 */
    private final MutableLiveData<Integer> currentPlayer = new MutableLiveData<>();

    /** 当前局数 */
    private final MutableLiveData<Integer> currentRound = new MutableLiveData<>(0);

    /** 每个玩家的骰子点数，dicePerPlayer[p][i] */
    private int[][] dicePerPlayer;

    /** 每个玩家的胜负记录，records[p][0]=胜局数，records[p][1]=负局数 */
    private final MutableLiveData<int[][]> winLossRecords = new MutableLiveData<>();

    /** 最终排名（按名次排列的玩家编号），null表示游戏未结束 */
    private final MutableLiveData<List<Integer>> ranking = new MutableLiveData<>(null);

    /** 当前叫数，null表示还没有叫数（开局的第一个玩家必须叫数） */
    private final MutableLiveData<Bid> currentBid = new MutableLiveData<>();

    /** 选择的叫数数量 */
    private final MutableLiveData<Integer> selectedQuantity = new MutableLiveData<>(1);

    /** 选择的叫数点数 */
    private final MutableLiveData<Integer> selectedFace = new MutableLiveData<>(2);

    /** 选择的叫数是否斋（true=斋，false=飞） */
    private final MutableLiveData<Boolean> selectedZhai = new MutableLiveData<>(false);

    /** 当前选择的叫数是否是合法的加码 */
    private final MutableLiveData<Boolean> bidValid = new MutableLiveData<>(false);

    /** 叫数选择器激活状态（你的回合且未在开骰） */
    private final MutableLiveData<Boolean> bidControlsEnabled = new MutableLiveData<>(false);

    /** 叫数按钮激活状态 */
    private final MutableLiveData<Boolean> bidButtonEnabled = new MutableLiveData<>(false);

    /** 开骰按钮激活状态 */
    private final MutableLiveData<Boolean> challengeButtonEnabled = new MutableLiveData<>(false);

    /** 开骰结果，非null时表示正在显示所有玩家的骰子 */
    private final MutableLiveData<RevealResult> revealResult = new MutableLiveData<>(null);

    /** 游戏日志 */
    private final MutableLiveData<List<Pair<Integer, Object[]>>> gameLog = new MutableLiveData<>(new ArrayList<>());

    /** 胜负记录（内部数组） */
    private int[][] records;

    /** 本轮先叫的玩家 */
    private int nextRoundStarter = PLAYER_HUMAN;

    /** 局数 */
    private int roundNumber = 0;

    /** 是否正在显示开骰结果 */
    private boolean revealing = false;

    public LiarsDiceGameViewModel() {
        super(NUM_DICE_PER_PLAYER, UNLIMITED_ROLLS);
        disableAllDice();
        this.numPlayers = DEFAULT_PLAYERS;
        initGame(numPlayers);
    }

    /**
     * 叫数，由数量、点数和是否斋组成
     */
    public static class Bid {
        /** 数量 */
        public final int quantity;

        /** 点数，1~6 */
        public final int face;

        /** 是否斋（1不作为万能） */
        public final boolean zhai;

        public Bid(int quantity, int face, boolean zhai) {
            this.quantity = quantity;
            this.face = face;
            this.zhai = zhai;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Bid))
                return false;
            Bid bid = (Bid) o;
            return quantity == bid.quantity && face == bid.face && zhai == bid.zhai;
        }

        @Override
        public int hashCode() {
            return 31 * (31 * quantity + face) + (zhai ? 1 : 0);
        }
    }

    /**
     * 开骰结果
     */
    public static class RevealResult {
        /** 玩家数量 */
        public final int numPlayers;

        /** 所有玩家的骰子点数 */
        public final int[][] dice;

        /** 被质疑的叫数 */
        public final Bid bid;

        /** 实际个数 */
        public final int actualCount;

        /** 质疑的玩家 */
        public final int challenger;

        /** 输家（本局输家） */
        public final int loser;

        /** 叫数是否属实（属实则质疑者输，否则上家输） */
        public final boolean bidTrue;

        RevealResult(int numPlayers, int[][] dice, Bid bid, int actualCount,
                     int challenger, int loser, boolean bidTrue) {
            this.numPlayers = numPlayers;
            this.dice = dice;
            this.bid = bid;
            this.actualCount = actualCount;
            this.challenger = challenger;
            this.loser = loser;
            this.bidTrue = bidTrue;
        }
    }

    public LiveData<Integer> getCurrentPlayer() {
        return currentPlayer;
    }

    public LiveData<Integer> getCurrentRound() {
        return currentRound;
    }

    public LiveData<int[][]> getWinLossRecords() {
        return winLossRecords;
    }

    public LiveData<List<Integer>> getRanking() {
        return ranking;
    }

    public LiveData<Bid> getCurrentBid() {
        return currentBid;
    }

    public LiveData<Integer> getSelectedQuantity() {
        return selectedQuantity;
    }

    public LiveData<Integer> getSelectedFace() {
        return selectedFace;
    }

    public LiveData<Boolean> getSelectedZhai() {
        return selectedZhai;
    }

    public LiveData<Boolean> getBidValid() {
        return bidValid;
    }

    public LiveData<Boolean> getBidControlsEnabled() {
        return bidControlsEnabled;
    }

    public LiveData<Boolean> getBidButtonEnabled() {
        return bidButtonEnabled;
    }

    public LiveData<Boolean> getChallengeButtonEnabled() {
        return challengeButtonEnabled;
    }

    public LiveData<RevealResult> getRevealResult() {
        return revealResult;
    }

    public LiveData<List<Pair<Integer, Object[]>>> getGameLog() {
        return gameLog;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public boolean isHumanTurn() {
        Integer player = currentPlayer.getValue();
        return player != null && player == PLAYER_HUMAN;
    }

    public boolean isComputerTurn() {
        Integer player = currentPlayer.getValue();
        return player != null && player != PLAYER_HUMAN;
    }

    /** 返回玩家名称的字符串资源id（电脑玩家使用固定名称） */
    public static int playerNameResId(int p) {
        if (p == PLAYER_HUMAN)
            return R.string.playerYou;
        return switch (p) {
            case 1 -> R.string.playerComputer1;
            case 2 -> R.string.playerComputer2;
            case 3 -> R.string.playerComputer3;
            default -> R.string.playerComputerN;
        };
    }

    /** 场上所有玩家骰子总数 */
    public int getTotalDice() {
        return numPlayers * NUM_DICE_PER_PLAYER;
    }

    /** 起叫个数（叫骰的最小数量） */
    public int getMinQuantity(boolean zhai) {
        return switch (numPlayers) {
            case 3 -> zhai ? 4 : 5;
            case 4 -> zhai ? 5 : 7;
            default -> 3;  // 2人：斋3、飞3
        };
    }

    /** 返回当前选择的叫数 */
    public Bid getSelectedBid() {
        Integer q = selectedQuantity.getValue();
        Integer f = selectedFace.getValue();
        Boolean z = selectedZhai.getValue();
        return new Bid(q == null ? 1 : q, f == null ? 2 : f, z != null && z);
    }

    /** 设置选择的叫数数量（范围：起叫个数~场上骰子总数） */
    public void setSelectedQuantity(int quantity) {
        int minQuantity = getMinQuantity(Boolean.TRUE.equals(selectedZhai.getValue()));
        if (quantity < minQuantity || quantity > getTotalDice())
            return;
        selectedQuantity.setValue(quantity);
        updateTurnButtons();
    }

    /** 设置选择的叫数点数 */
    public void setSelectedFace(int face) {
        if (face < 1 || face > 6)
            return;
        selectedFace.setValue(face);
        if (face == 1)
            selectedZhai.setValue(true);  // 叫1点默认斋
        updateTurnButtons();
    }

    /** 设置选择的叫数是否斋（true=斋，false=飞） */
    public void setSelectedZhai(boolean zhai) {
        if (selectedFace.getValue() == 1)
            zhai = true;  // 叫1点时只能斋
        selectedZhai.setValue(zhai);
        // 若数量低于新模式的最小值，自动提高到最小值
        int minQuantity = getMinQuantity(zhai);
        Integer quantity = selectedQuantity.getValue();
        if (quantity != null && quantity < minQuantity)
            selectedQuantity.setValue(minQuantity);
        updateTurnButtons();
    }

    /** 翻转选择的叫数是否斋 */
    public void toggleSelectedZhai() {
        Boolean zhai = selectedZhai.getValue();
        setSelectedZhai(zhai == null || !zhai);
    }

    /** 人类玩家叫数 */
    public void makeBid(Bid bid) {
        if (!isHumanTurn() || revealing)
            return;
        doBid(bid);
    }

    /** 人类玩家喊开（质疑上家的叫数） */
    public void challenge() {
        if (!isHumanTurn() || revealing)
            return;
        if (currentBid.getValue() == null)
            return;
        doChallenge(PLAYER_HUMAN);
    }

    /** 继续游戏（关闭开骰结果对话框后） */
    public void continueAfterReveal() {
        if (!revealing)
            return;
        revealing = false;
        revealResult.setValue(null);
        unlockAllDice();

        if (roundNumber >= TOTAL_ROUNDS) {
            // 所有局结束，计算最终排名
            addLog(R.string.logSeparator);
            List<Integer> order = computeRanking();
            addLog(order.get(0) == PLAYER_HUMAN ? R.string.logYouWin : R.string.logPlayerNWins, order.get(0));
            ranking.setValue(order);
            return;
        }
        startRound();
    }

    /** 以相同人数重新开始新游戏 */
    @Override
    public void reset() {
        initGame(numPlayers);
    }

    /** 以指定玩家数开始新游戏 */
    public void newGame(int numPlayers) {
        if (numPlayers < MIN_PLAYERS || numPlayers > MAX_PLAYERS)
            throw new IllegalArgumentException("玩家数量必须在2~4之间");
        this.numPlayers = numPlayers;
        initGame(numPlayers);
    }

    /**
     * 判断叫数是否合法（数量在起叫个数~场上骰子总数之间，点数在1~6之间，叫1点时必须是斋）
     */
    public boolean isBidValid(Bid bid) {
        return bid != null && bid.quantity >= getMinQuantity(bid.zhai)
                && bid.quantity <= getTotalDice()
                && bid.face >= 1 && bid.face <= 6
                && (bid.face >= 2 || bid.zhai);
    }

    /**
     * 判断叫数next是否是叫数prev的合法加码<br>
     * prev为null表示开叫，任何合法叫数都可以<br>
     * 规则：<br>
     * 同模式（不改变斋/飞）：数量更大（不要求点数不变），或同数量点数更大（1为最高点数）<br>
     * 斋→飞：数量至少+2<br>
     * 飞→斋：数量至少-1
     */
    public boolean isBidRaiseValid(Bid prev, Bid next) {
        if (!isBidValid(next))
            return false;
        if (prev == null)
            return true;
        if (!isBidValid(prev))
            return false;
        if (prev.zhai == next.zhai) {
            // 同模式：数量更大，或同数量点数更大
            if (next.quantity > prev.quantity)
                return true;
            if (next.quantity == prev.quantity && faceRank(next.face) > faceRank(prev.face))
                return true;
            return false;
        }
        if (prev.zhai) {
            // 斋→飞：数量至少+2
            return next.quantity >= prev.quantity + 2;
        }
        // 飞→斋：数量至少-1
        return next.quantity >= prev.quantity - 1;
    }

    /** 点数大小：1为最高，2~6按数值 */
    static int faceRank(int face) {
        return face == 1 ? 6 : face - 1;
    }

    /** 返回下一个更高的点数（1为最高点数） */
    private static int nextHigherFace(int face) {
        return face == 6 ? 1 : face + 1;
    }

    /** 计算所有玩家骰子中满足叫数的实际个数（按万能规则） */
    public int countBid(int face, boolean zhai) {
        int count = 0;
        for (int p = 0; p < numPlayers; p++) {
            for (int i = 0; i < NUM_DICE_PER_PLAYER; i++) {
                int d = dicePerPlayer[p][i];
                if (d == 0)
                    continue;
                if (zhai) {
                    if (d == face)
                        count++;
                }
                else if (face == 1) {
                    if (d == 1)
                        count++;
                }
                else {
                    if (d == face || d == 1)  // 1为万能
                        count++;
                }
            }
        }
        return count;
    }

    /** 骰子点数d在叫数中是否计入（用于开骰时高亮对应骰子） */
    static boolean shouldLock(int d, int bidFace, boolean zhai) {
        if (d == bidFace)
            return true;
        return !zhai && bidFace != 1 && d == 1;  // 万能1也算
    }

    /** 执行叫数（校验合法性后更新状态并轮到下家） */
    void doBid(Bid bid) {
        Bid prev = currentBid.getValue();
        if (!isBidRaiseValid(prev, bid))
            return;
        currentBid.setValue(bid);
        addLog(bid.zhai ? R.string.logBidZhai : R.string.logBidFei,
                currentPlayer.getValue(), bid.quantity, bid.face);
        int next = getNextPlayer(currentPlayer.getValue());
        currentPlayer.setValue(next);
        addLog(R.string.logPlayerTurn, next);
        startTurn();
    }

    /** 执行开骰判定：challenger质疑当前叫数 */
    void doChallenge(int challenger) {
        if (revealing)
            return;
        Bid bid = currentBid.getValue();
        if (bid == null)
            return;
        int prevBidder = getPreviousPlayer(challenger);
        addLog(R.string.logOpen, challenger, prevBidder);
        int actual = countBid(bid.face, bid.zhai);
        boolean bidTrue = actual >= bid.quantity;
        // 叫数属实则质疑者输，否则上家输
        int loser = bidTrue ? challenger : prevBidder;
        int winner = bidTrue ? prevBidder : challenger;

        // 只对开骰双方统计胜负：输家记一负，赢家记一胜，其余玩家不变
        records[loser][1]++;
        records[winner][0]++;
        winLossRecords.setValue(copyRecords());

        // 下一局由输家先叫
        nextRoundStarter = loser;

        revealing = true;
        addLog(bidTrue ? R.string.logBidTrue : R.string.logBidFalse, loser, actual, bid.face);
        addLog(R.string.logLoseRound, loser);

        // 高亮人类骰子窗口中对应点数的骰子
        setHumanDiceLockedForReveal(bid);

        bidControlsEnabled.setValue(false);
        bidButtonEnabled.setValue(false);
        challengeButtonEnabled.setValue(false);

        revealResult.setValue(new RevealResult(
                numPlayers, copyDice(), bid, actual, challenger, loser, bidTrue));
    }

    /** 计算机玩家回合 */
    protected void computerTurn() {
        if (revealing || !isComputerTurn())
            return;
        Bid bid = currentBid.getValue();
        if (bid == null) {
            doBid(chooseOpeningBid());
        }
        else if (shouldChallenge(bid)) {
            doChallenge(currentPlayer.getValue());
        }
        else {
            Bid raise = chooseRaiseBid(bid);
            if (raise == null)
                doChallenge(currentPlayer.getValue());  // 找不到合法加码时只能质疑
            else
                doBid(raise);
        }
    }

    // ---------- 开局和回合流程 ----------

    /** 初始化新游戏 */
    private void initGame(int numPlayers) {
        this.numPlayers = numPlayers;
        dicePerPlayer = new int[numPlayers][NUM_DICE_PER_PLAYER];
        records = new int[numPlayers][2];
        currentPlayer.setValue(PLAYER_HUMAN);
        nextRoundStarter = PLAYER_HUMAN;
        roundNumber = 0;
        currentRound.setValue(0);
        winLossRecords.setValue(copyRecords());
        ranking.setValue(null);
        currentBid.setValue(null);
        revealResult.setValue(null);
        revealing = false;
        gameLog.setValue(new ArrayList<>());
        addLog(R.string.logGameBegins);
        startRound();
    }

    /** 开始新一局：所有玩家掷骰子，重置叫数 */
    protected void startRound() {
        roundNumber++;
        if (roundNumber > 1)
            addLog(R.string.logSeparator);
        currentRound.setValue(roundNumber);
        rollAllPlayers();
        currentPlayer.setValue(nextRoundStarter);
        currentBid.setValue(null);
        addLog(R.string.logRoundBegins, roundNumber);
        addLog(R.string.logPlayerTurn, currentPlayer.getValue());
        startTurn();
    }

    /** 开始当前玩家的回合 */
    private void startTurn() {
        Integer player = currentPlayer.getValue();
        if (player == null)
            return;
        if (player == PLAYER_HUMAN) {
            setDefaultSelectedBid();
        }
        else {
            handler.postDelayed(this::computerTurn, DELAY);
        }
        updateTurnButtons();
    }

    /** 更新回合相关按钮状态 */
    private void updateTurnButtons() {
        Integer player = currentPlayer.getValue();
        boolean humanTurn = player != null && player == PLAYER_HUMAN && !revealing;
        Bid prev = currentBid.getValue();
        boolean valid = isBidRaiseValid(prev, getSelectedBid());
        bidValid.setValue(valid);
        bidControlsEnabled.setValue(humanTurn);
        bidButtonEnabled.setValue(humanTurn && valid);
        challengeButtonEnabled.setValue(humanTurn && prev != null);
    }

    /** 设置人类玩家回合的默认叫数选择 */
    private void setDefaultSelectedBid() {
        Bid prev = currentBid.getValue();
        if (prev == null) {
            // 开叫：选择自己手中有效个数最多的点数
            int[] faceCount = new int[7];
            for (int d : dicePerPlayer[PLAYER_HUMAN])
                faceCount[d]++;
            int bestFace = 1, bestCount = faceCount[1];
            for (int f = 2; f <= 6; f++) {
                int c = faceCount[f] + faceCount[1];  // 1为万能
                if (c > bestCount) {
                    bestCount = c;
                    bestFace = f;
                }
            }
            boolean zhai = bestFace == 1;
            selectedQuantity.setValue(Math.max(getMinQuantity(zhai), Math.max(1, bestCount)));
            selectedFace.setValue(bestFace);
            selectedZhai.setValue(zhai);  // 叫1点默认斋
        }
        else {
            int maxQuantity = getTotalDice();
            if (prev.quantity < maxQuantity) {
                // 最小加码：同模式、数量+1
                selectedQuantity.setValue(prev.quantity + 1);
                selectedFace.setValue(prev.face);
                selectedZhai.setValue(prev.zhai);
            }
            else {
                // 数量已到上限，只能同数量提高点数
                selectedQuantity.setValue(prev.quantity);
                selectedFace.setValue(nextHigherFace(prev.face));
                selectedZhai.setValue(prev.zhai);
            }
        }
        if (selectedFace.getValue() == 1)
            selectedZhai.setValue(true);
    }

    /** 所有玩家掷骰子 */
    private void rollAllPlayers() {
        for (int p = 0; p < numPlayers; p++)
            for (int i = 0; i < NUM_DICE_PER_PLAYER; i++)
                dicePerPlayer[p][i] = random.nextInt(6) + 1;
        updateHumanDiceWindow();
    }

    /** 更新人类玩家的骰子窗口 */
    private void updateHumanDiceWindow() {
        diceNumbers.setValue(dicePerPlayer[PLAYER_HUMAN].clone());
        unlockAllDice();
    }

    /** 开骰时锁定人类骰子窗口中对应点数的骰子 */
    private void setHumanDiceLockedForReveal(Bid bid) {
        boolean[] locked = new boolean[NUM_DICE_PER_PLAYER];
        for (int i = 0; i < NUM_DICE_PER_PLAYER; i++)
            locked[i] = shouldLock(dicePerPlayer[PLAYER_HUMAN][i], bid.face, bid.zhai);
        diceLocked.setValue(locked);
    }

    /** 返回下一个玩家 */
    private int getNextPlayer(int player) {
        return (player + 1) % numPlayers;
    }

    /** 返回上一个玩家（上家） */
    private int getPreviousPlayer(int player) {
        return (player - 1 + numPlayers) % numPlayers;
    }

    /** 计算最终排名：获胜局数最多、输的局数最少者优先 */
    private List<Integer> computeRanking() {
        List<Integer> order = new ArrayList<>();
        for (int p = 0; p < numPlayers; p++)
            order.add(p);
        order.sort((a, b) -> {
            if (records[b][0] != records[a][0])
                return records[b][0] - records[a][0];  // 胜局多者优先
            return records[a][1] - records[b][1];       // 败局少者优先
        });
        return order;
    }

    /** 返回骰子点数的深拷贝 */
    private int[][] copyDice() {
        int[][] copy = new int[numPlayers][];
        for (int p = 0; p < numPlayers; p++)
            copy[p] = dicePerPlayer[p].clone();
        return copy;
    }

    /** 返回胜负记录的深拷贝 */
    private int[][] copyRecords() {
        int[][] copy = new int[numPlayers][2];
        for (int p = 0; p < numPlayers; p++)
            System.arraycopy(records[p], 0, copy[p], 0, 2);
        return copy;
    }

    /** 添加一条游戏日志 */
    protected void addLog(int resId, Object... args) {
        var log = gameLog.getValue();
        if (log == null)
            return;
        log.add(Pair.create(resId, args));
        gameLog.setValue(log);
    }

    // ---------- 计算机玩家AI ----------

    /** 每个骰子命中指定叫数的概率 */
    private static double probabilityPerDie(int face, boolean zhai) {
        if (zhai || face == 1)
            return 1.0 / 6;
        return 2.0 / 6;  // 点数本身 + 万能1
    }

    /** 当前玩家自己的骰子中满足叫数的个数 */
    private int countOwn(int face, boolean zhai) {
        int p = currentPlayer.getValue();
        int count = 0;
        for (int i = 0; i < NUM_DICE_PER_PLAYER; i++) {
            int d = dicePerPlayer[p][i];
            if (d == 0)
                continue;
            if (zhai) {
                if (d == face)
                    count++;
            }
            else if (face == 1) {
                if (d == 1)
                    count++;
            }
            else {
                if (d == face || d == 1)
                    count++;
            }
        }
        return count;
    }

    /** 计算机玩家选择开叫 */
    private Bid chooseOpeningBid() {
        int p = currentPlayer.getValue();
        int[] faceCount = new int[7];
        for (int d : dicePerPlayer[p])
            faceCount[d]++;
        int bestFace = 1, bestCount = faceCount[1];
        for (int f = 2; f <= 6; f++) {
            int c = faceCount[f] + faceCount[1];
            if (c > bestCount) {
                bestCount = c;
                bestFace = f;
            }
        }
        boolean zhai = bestFace == 1;
        int q = Math.max(getMinQuantity(zhai), Math.max(1, bestCount));
        return new Bid(q, bestFace, zhai);  // 叫1点默认斋
    }

    /** 计算机玩家是否应该质疑当前叫数 */
    private boolean shouldChallenge(Bid bid) {
        double prob = bidTrueProbability(bid);
        double threshold = 0.35 + random.nextDouble() * 0.25;  // 0.35~0.6
        return prob < threshold;
    }

    /** 估计叫数属实（实际个数 >= 数量）的概率 */
    private double bidTrueProbability(Bid bid) {
        int own = countOwn(bid.face, bid.zhai);
        int oppDice = getTotalDice() - NUM_DICE_PER_PLAYER;
        double p = probabilityPerDie(bid.face, bid.zhai);
        double mean = own + p * oppDice;
        double sd = Math.sqrt(oppDice * p * (1 - p));
        if (sd == 0)
            return bid.quantity <= own ? 1.0 : 0.0;
        double z = (bid.quantity - 0.5 - mean) / sd;  // 连续性校正
        return 1 - normalCdf(z);
    }

    /** 计算机玩家选择加码的叫数，找不到合法加码时返回null */
    private Bid chooseRaiseBid(Bid prev) {
        int maxQuantity = getTotalDice();
        List<Bid> candidates = new ArrayList<>();
        for (int q = 1; q <= maxQuantity; q++) {
            for (int f = 2; f <= 6; f++) {
                for (boolean zhai : new boolean[] {false, true}) {
                    Bid b = new Bid(q, f, zhai);
                    if (isBidRaiseValid(prev, b))
                        candidates.add(b);
                }
            }
            Bid b1 = new Bid(q, 1, true);  // 叫1点必须是斋
            if (isBidRaiseValid(prev, b1))
                candidates.add(b1);
        }
        Bid best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Bid b : candidates) {
            double prob = bidTrueProbability(b);
            // 数量增加越多得分越低，偏向小幅加码
            double score = prob - 0.2 * Math.max(0, b.quantity - prev.quantity)
                    + random.nextDouble() * 0.03;
            if (score > bestScore) {
                bestScore = score;
                best = b;
            }
        }
        return best;
    }

    /** 标准正态分布累积分布函数 */
    private static double normalCdf(double z) {
        return 0.5 * (1 + erf(z / Math.sqrt(2)));
    }

    /** 误差函数近似 */
    private static double erf(double x) {
        double sign = x < 0 ? -1 : 1;
        x = Math.abs(x);
        double t = 1 / (1 + 0.3275911 * x);
        double y = 1 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t
                - 0.284496736) * t + 0.254829592) * t * Math.exp(-x * x);
        return sign * y;
    }

    // ---------- 测试辅助方法 ----------

    /** 设置所有玩家的骰子点数（用于测试） */
    void setDiceForTest(int[][] dice) {
        for (int p = 0; p < numPlayers; p++)
            System.arraycopy(dice[p], 0, dicePerPlayer[p], 0, NUM_DICE_PER_PLAYER);
        updateHumanDiceWindow();
    }
}
